package nova.mjs.mentor.profile.dto;

import lombok.*;

// 선배들의 프로필 상단 4개의 통계 카드
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorStatsDTO {
    private long mentorCount;
    private long jobCategoryCount;     // 현재 스키마엔 '직무 카테고리'가 별도로 없어 0으로 응답(확장 여지)
    private long totalConsultations;   // 멘토링 총 카운트
    private Integer averageResponseRate; // 추후 도입 전까지 null
}
