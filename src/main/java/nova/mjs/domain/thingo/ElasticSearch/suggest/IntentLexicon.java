package nova.mjs.domain.thingo.ElasticSearch.suggest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * IntentLexicon (운영 안전형)
 *
 * 목적:
 * - intent 기반 자동완성/연관어/가중치 정책을 제공한다.
 * - 리소스 파일 누락/파싱 실패 시에도 "서버는 절대 죽지 않고" fallback 모드로 동작한다.
 *
 * 핵심 정책:
 * - 1글자에서도 intent prefix 매칭은 허용한다. (사용자 체감 품질을 위해)
 * - completion 최소 prefix 길이(meta.minCompletionPrefixLength)는 SuggestService에서 사용한다.
 *   (즉, Lexicon은 "의도 사전" 역할에 집중하고, completion 호출 여부는 검색 서비스가 제어한다.)
 */
@Slf4j
@Component
public class IntentLexicon {

    private static final String LEXICON_PATH = "search/intent_lexicon.json";

    /**
     * 운영 안전 기본값
     * - minCompletionPrefixLength 기본값을 1로 둔다.
     *   이유: lexicon 파일이 없거나 로딩 실패 시에도 1글자 intent 추천은 동작해야 사용자 체감이 좋다.
     *   (completion 최소 prefix 정책은 SuggestService에서 별도로 통제 가능)
     */
    private static final LexiconMeta DEFAULT_META = new LexiconMeta(1, List.of(), Map.of());

    private final ObjectMapper objectMapper;

    /**
     * 실패 대비 기본값
     * - entries: 비어있으면 lexicon 기능은 꺼지고, 검색은 그대로 동작
     * - meta: DEFAULT_META로 fallback
     */
    private List<IntentLexiconEntry> entries = List.of();
    private LexiconMeta meta = DEFAULT_META;

    public IntentLexicon(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 애플리케이션 시작 후 로딩
     * - 실패해도 서버는 정상 실행
     */
    @PostConstruct
    void init() {
        try {
            load();
            log.info("[IntentLexicon] loaded {} entries (minCompletionPrefixLength={})",
                    entries.size(), meta.minCompletionPrefixLength());
        } catch (Exception e) {
            log.error("[IntentLexicon] load failed -> fallback mode (search still works)", e);
            this.entries = List.of();
            this.meta = DEFAULT_META;
        }
    }

    private void load() throws Exception {
        ClassPathResource resource = new ClassPathResource(LEXICON_PATH);

        if (!resource.exists()) {
            log.warn("[IntentLexicon] {} not found -> fallback mode", LEXICON_PATH);
            this.entries = List.of();
            this.meta = DEFAULT_META;
            return;
        }

        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String json = new String(bytes, StandardCharsets.UTF_8);

            IntentLexiconFile file = objectMapper.readValue(json, IntentLexiconFile.class);

            this.entries = Optional.ofNullable(file.entries()).orElse(List.of());
            this.meta = Optional.ofNullable(file.meta()).orElse(DEFAULT_META);

            // 운영 안전: 잘못된 값이 들어오면 최소 1로 클램프
            if (this.meta.minCompletionPrefixLength() < 1) {
                this.meta = new LexiconMeta(1, this.meta.globalStopwords(), this.meta.baseTypeWeight());
            }
        }
    }

    /* ========================= 문서 매칭 ========================= */

    /**
     * 문서(title/content) 기반으로 lexicon entry를 매칭한다.
     * - 없으면 Optional.empty()
     * - score가 가장 높은 entry를 반환한다.
     */
    public Optional<IntentMatch> matchDocument(String title, String content) {
        if (entries.isEmpty()) {
            return Optional.empty();
        }

        String t = safeLower(title);
        String c = safeLower(content);

        return entries.stream()
                .map(e -> scoreEntry(e, t, c))
                .filter(m -> m.score > 0)
                .max(Comparator.comparingInt(m -> m.score));
    }

    /* ========================= prefix 매칭 ========================= */

    /**
     * 사용자 입력 prefix로 intent 후보를 매칭한다.
     *
     * 정책:
     * - intent는 1글자도 허용한다. (사용자 체감)
     * - completion 최소 prefix 길이는 SuggestService에서 판단한다. (역할 분리)
     */
    public Optional<IntentLexiconEntry> matchPrefix(String rawPrefix) {
        if (entries.isEmpty()) {
            return Optional.empty();
        }

        String prefix = safeLower(rawPrefix).trim();
        if (prefix.isEmpty()) {
            return Optional.empty();
        }

        // intent는 1글자도 허용 (completion 정책과 분리)
        if (prefix.length() < 1) {
            return Optional.empty();
        }

        return entries.stream()
                .filter(e -> matchesPrefix(e, prefix))
                .max(Comparator.comparingInt(IntentLexiconEntry::weight));
    }

    private boolean matchesPrefix(IntentLexiconEntry e, String prefix) {
        for (String k : nullSafe(e.matchKeywords())) {
            String key = safeLower(k);

            if (key.isEmpty()) {
                continue;
            }

            // 1글자 prefix는 "첫 글자"만 일치해도 허용
            if (prefix.length() == 1) {
                if (key.charAt(0) == prefix.charAt(0)) {
                    return true;
                }
                continue;
            }

            if (key.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /* ========================= scoring ========================= */

    /**
     * 문서 매칭 점수 계산
     * - title에 키워드 포함: 강한 가중치
     * - content에 키워드 포함: 약한 가중치
     * - entry 자체 weight 추가
     */
    private IntentMatch scoreEntry(IntentLexiconEntry e, String title, String content) {
        int score = 0;

        for (String k : nullSafe(e.matchKeywords())) {
            String key = safeLower(k);
            if (key.isEmpty()) {
                continue;
            }

            if (title.contains(key)) {
                score += 120;
            } else if (content.contains(key)) {
                score += 20;
            }
        }

        score += e.weight();
        return new IntentMatch(e, score);
    }

    /* ========================= util ========================= */

    private String safeLower(String v) {
        return v == null ? "" : v.toLowerCase();
    }

    private List<String> nullSafe(List<String> v) {
        return v == null ? List.of() : v;
    }

    /**
     * meta는 SuggestService 등 외부에서 읽기 전용으로 사용
     */
    public LexiconMeta meta() {
        return meta;
    }

    /* ========================= records ========================= */

    public record IntentMatch(IntentLexiconEntry entry, int score) {}

    public record IntentLexiconFile(int version, LexiconMeta meta, List<IntentLexiconEntry> entries) {}

    /**
     * LexiconMeta
     *
     * minCompletionPrefixLength:
     * - completion suggester를 호출하기 위한 "최소 prefix 길이" 정책 값
     * - 실제 적용은 SuggestService가 담당한다.
     */
    public record LexiconMeta(
            int minCompletionPrefixLength,
            List<String> globalStopwords,
            Map<String, Integer> baseTypeWeight
    ) {}
}
