package nova.mjs.domain.thingo.semantic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 결정 가능한 표현만 분류하는 보수적인 1차 규칙 분류기.
 * 검색 점수나 알림 발송 정책은 포함하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class NoticeSemanticClassifier {

    private static final int BODY_EVIDENCE_LIMIT = 1_200;
    /**
     * 본문에는 다른 프로그램의 예시·참고 문구가 자주 섞인다. 제목에 토픽 근거가 없을 때는
     * K-Move 같은 고유 프로그램명 수준(130)만 보조 근거로 허용한다.
     */
    private static final int BODY_ALIAS_MIN_PRIORITY = 130;

    private final TopicCatalog topicCatalog;

    public NoticeSemanticMetadata classify(String title, String content, Instant deadline) {
        String safeTitle = safe(title);
        String bodyEvidence = leadingEvidence(content);
        String bodyTopicEvidence = removeReferenceOnlyEvidence(bodyEvidence);
        String evidenceText = (safeTitle + " " + bodyEvidence).trim();

        Set<String> directTopicIds = topicCatalog.resolve(safeTitle).stream()
                .map(match -> match.topic().topicId())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (isPersonnelRecruitment(safeTitle)) {
            directTopicIds.clear();
        }
        boolean topicFoundInTitle = !directTopicIds.isEmpty();
        if (directTopicIds.isEmpty()
                && !isPersonnelRecruitment(safeTitle)
                && !isDomesticReturnSupport(safeTitle)) {
            topicCatalog.resolve(bodyTopicEvidence, BODY_ALIAS_MIN_PRIORITY).stream()
                    .map(match -> match.topic().topicId())
                    .forEach(directTopicIds::add);
        }
        Set<String> expandedTopicIds = topicCatalog.withAncestors(directTopicIds);

        double confidence = expandedTopicIds.isEmpty() ? 0.40d : topicFoundInTitle ? 0.95d : 0.70d;

        return new NoticeSemanticMetadata(
                Set.copyOf(directTopicIds),
                expandedTopicIds,
                classifyEventType(safeTitle, bodyEvidence),
                classifyAudiences(evidenceText),
                classifyCampuses(evidenceText),
                deadline,
                topicCatalog.version(),
                ClassificationSource.RULE,
                confidence
        );
    }

    private NoticeEventType classifyEventType(String title, String bodyEvidence) {
        NoticeEventType titleType = classifyEventType(title);
        if (containsAny(title, "후기", "성과 발표", "성과보고", "성료", "활동보고")) {
            return NoticeEventType.INFORMATION;
        }
        if (titleType != NoticeEventType.INFORMATION && titleType != NoticeEventType.UNKNOWN) {
            return titleType;
        }
        NoticeEventType bodyType = classifyEventType(bodyEvidence);
        return bodyType == NoticeEventType.UNKNOWN ? titleType : bodyType;
    }

    private NoticeEventType classifyEventType(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        if (containsAny(normalized, "취소", "철회")) return NoticeEventType.CANCELLATION;
        if (normalized.contains("폐지") && !containsAny(normalized, "신청", "절차")) {
            return NoticeEventType.CANCELLATION;
        }
        if (containsAny(normalized, "중단", "중지")) return NoticeEventType.SUSPENSION;
        if (containsAny(normalized, "연장", "기한 연기")) return NoticeEventType.EXTENSION;
        if (containsAny(normalized, "결과", "합격자", "선발자")) return NoticeEventType.RESULT;
        if (containsAny(normalized, "변경", "정정")) return NoticeEventType.CHANGE;
        if (containsAny(normalized, "추가")) return NoticeEventType.ADDITION;
        if (containsAny(normalized, "모집", "채용", "선발")) return NoticeEventType.RECRUITMENT;
        if (containsAny(normalized, "신청", "접수")) return NoticeEventType.APPLICATION;
        return text.isBlank() ? NoticeEventType.UNKNOWN : NoticeEventType.INFORMATION;
    }

    private Set<NoticeAudience> classifyAudiences(String text) {
        LinkedHashSet<NoticeAudience> result = new LinkedHashSet<>();
        if (containsAny(text, "전체 학생", "전교생", "모든 학생")) result.add(NoticeAudience.ALL);
        if (text.contains("재학생")) result.add(NoticeAudience.ENROLLED_STUDENT);
        if (text.contains("신입생")) result.add(NoticeAudience.NEW_STUDENT);
        if (text.contains("편입생")) result.add(NoticeAudience.TRANSFER_STUDENT);
        if (text.contains("졸업예정자")) result.add(NoticeAudience.GRADUATION_CANDIDATE);
        if (text.contains("졸업생")) result.add(NoticeAudience.GRADUATE);
        return Set.copyOf(result);
    }

    private Set<NoticeCampus> classifyCampuses(String text) {
        LinkedHashSet<NoticeCampus> result = new LinkedHashSet<>();
        if (containsAny(text, "양 캠퍼스", "전체 캠퍼스")) result.add(NoticeCampus.ALL);
        if (containsAny(text, "인문캠퍼스", "서울캠퍼스")) result.add(NoticeCampus.SEOUL);
        if (containsAny(text, "자연캠퍼스", "용인캠퍼스")) result.add(NoticeCampus.YONGIN);
        return Set.copyOf(result);
    }

    private boolean containsAny(String text, String... candidates) {
        for (String candidate : candidates) {
            if (text.contains(candidate)) return true;
        }
        return false;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String leadingEvidence(String content) {
        String safeContent = safe(content).trim();
        return safeContent.length() <= BODY_EVIDENCE_LIMIT
                ? safeContent
                : safeContent.substring(0, BODY_EVIDENCE_LIMIT);
    }

    /** 부서명에 들어간 '취업지원'을 학생 대상 취업 Topic으로 오인하지 않는다. */
    private boolean isPersonnelRecruitment(String title) {
        return containsAny(title, "직원", "교직원", "계약직", "기간제", "전담인력")
                && containsAny(title, "채용", "모집");
    }

    private String removeReferenceOnlyEvidence(String bodyEvidence) {
        String compact = bodyEvidence.stripLeading();
        if (containsAny(compact, "관련 공지:", "관련공지:", "참고 링크:", "참고링크:")) {
            return "";
        }
        return bodyEvidence;
    }

    /** 해외경험 이력만 조건으로 삼는 국내 재취업 공지는 새로운 해외기회가 아니다. */
    private boolean isDomesticReturnSupport(String title) {
        return containsAny(title, "국내 재취업", "국내재취업");
    }
}
