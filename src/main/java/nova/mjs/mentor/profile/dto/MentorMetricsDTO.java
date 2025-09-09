package nova.mjs.mentor.profile.dto;

import lombok.*;

// 상세 지표
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorMetricsDTO {
    private long viewCount;       // 조회수(234)
    private long thanksCount;     // 감사 인사(12)
    private long mentoringCount;  // 멘토링(28)
    private Boolean bookmarked;   // 로그인 사용자 기준(없으면 null)
}
