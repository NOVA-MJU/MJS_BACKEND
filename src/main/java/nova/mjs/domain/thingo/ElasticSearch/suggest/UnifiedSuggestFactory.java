package nova.mjs.domain.thingo.ElasticSearch.suggest;

import lombok.RequiredArgsConstructor;
import nova.mjs.config.elasticsearch.KomoranTokenizerUtil;
import nova.mjs.domain.thingo.ElasticSearch.Document.SearchDocument;
import nova.mjs.domain.thingo.ElasticSearch.suggest.IntentLexicon.IntentMatch;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * UnifiedSuggestFactory
 *
 * 목표:
 * - completion suggester에 넣을 input을 "사용자 관점"으로 정제한다.
 * - 개최/안내/모집 같은 범용 토큰은 기본적으로 제외한다.
 * - IntentLexicon 기반으로 학사 핵심 intent에 높은 weight를 부여한다.
 */
@Component
@RequiredArgsConstructor
public class UnifiedSuggestFactory {

    private static final int MAX_INPUTS = 10;
    private static final int MAX_INPUT_LENGTH = 50;

    /**
     * 본문 전체 형태소 분석은 비용이 크므로 "앞부분 일부만" 사용
     * - 제목에 없는 핵심 키워드(예: 특공대)가 본문에만 있을 때 보완
     */
    private static final int CONTENT_SNIPPET_LENGTH = 200;

    private final IntentLexicon intentLexicon;

    /**
     * UnifiedSearchDocument.suggest 로 저장할 Completion 생성
     */
    public Completion create(SearchDocument doc) {
        String title = safe(doc.getTitle());
        String content = safe(doc.getContent());

        Optional<IntentMatch> match = intentLexicon.matchDocument(title, content);

        LinkedHashSet<String> inputs = new LinkedHashSet<>();

        // 1) 타이틀 자체(가장 강력)
        addIfValid(inputs, normalizeTitleForSuggest(title));

        // 2) title 형태소 토큰
        addTokens(inputs, KomoranTokenizerUtil.generateSuggestions(title));

        // 3) content 스니펫 토큰(제목에 없는 단어 보완)
        String snippet = content.length() > CONTENT_SNIPPET_LENGTH
                ? content.substring(0, CONTENT_SNIPPET_LENGTH)
                : content;
        addTokens(inputs, KomoranTokenizerUtil.generateSuggestions(snippet));

        // 4) intent expansions(학사 intent 보강)
        if (match.isPresent()) {
            IntentLexiconEntry e = match.get().entry();
            for (String ex : nullSafe(e.expansions())) {
                addIfValid(inputs, ex);
                if (inputs.size() >= MAX_INPUTS) {
                    break;
                }
            }
        }

        int weight = computeWeight(doc, match);

        Completion completion = new Completion(new ArrayList<>(inputs));
        completion.setWeight(weight);
        return completion;
    }

    private void addTokens(Set<String> inputs, List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        for (String t : tokens) {
            addIfValid(inputs, t);
            if (inputs.size() >= MAX_INPUTS) {
                return;
            }
        }
    }

    /**
     * weight 정책
     * - type 기본값 + intent 가중치 + 최신성 + 인기 신호
     * - completion.weight는 int 이므로 최종 클램프
     */
    private int computeWeight(SearchDocument doc, Optional<IntentMatch> match) {
        int base = baseTypeWeight(doc.getType());
        int intentWeight = match.map(m -> m.entry().weight()).orElse(0);

        int recencyBoost = recencyBoost(doc.getInstant());
        int popularityBoost = popularityBoost(doc.getLikeCount(), doc.getCommentCount());

        int total = base + intentWeight + recencyBoost + popularityBoost;
        return clamp(total, 1, 300);
    }

    private int baseTypeWeight(String type) {
        Map<String, Integer> map = intentLexicon.meta().baseTypeWeight();
        if (map == null) {
            return 30;
        }
        return map.getOrDefault(type, 30);
    }

    /**
     * 최신성 부스트
     * - 최근 7일: +30
     * - 최근 30일: +15
     * - 최근 90일: +5
     *
     * 안전장치:
     * - 미래 시각이 들어오면 days가 음수가 될 수 있으니 0으로 클램프
     */
    private int recencyBoost(Instant instant) {
        if (instant == null) {
            return 0;
        }

        long days = Duration.between(instant, Instant.now()).toDays();
        if (days < 0) {
            days = 0;
        }

        if (days <= 7) return 30;
        if (days <= 30) return 15;
        if (days <= 90) return 5;
        return 0;
    }

    private int popularityBoost(Integer like, Integer comment) {
        int l = like == null ? 0 : like;
        int c = comment == null ? 0 : comment;
        int score = (l * 2) + (c * 3);
        return clamp(score, 0, 40);
    }

    /**
     * suggest input 정규화
     * - [학부·대학원] 같은 헤더 제거
     * - 길이 제한
     */
    private String normalizeTitleForSuggest(String title) {
        String v = safe(title).trim();
        v = v.replaceAll("^\\[[^\\]]+\\]\\s*", "");

        if (v.length() > MAX_INPUT_LENGTH) {
            v = v.substring(0, MAX_INPUT_LENGTH);
        }
        return v;
    }

    private void addIfValid(Set<String> inputs, String raw) {
        String v = safe(raw).trim();
        if (v.isEmpty()) {
            return;
        }

        if (v.length() > MAX_INPUT_LENGTH) {
            return;
        }

        // completion input은 1글자 단독이 노이즈가 되기 쉬움(1글자 허용은 intent prefix에서 담당)
        if (v.length() <= 1) {
            return;
        }

        // 숫자만 있는 토큰 제외
        if (v.chars().allMatch(Character::isDigit)) {
            return;
        }

        // 글로벌 stopwords(정확히 일치)
        for (String sw : nullSafe(intentLexicon.meta().globalStopwords())) {
            if (v.equals(sw)) {
                return;
            }
        }

        inputs.add(v);
    }

    private List<String> nullSafe(List<String> v) {
        return v == null ? List.of() : v;
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
