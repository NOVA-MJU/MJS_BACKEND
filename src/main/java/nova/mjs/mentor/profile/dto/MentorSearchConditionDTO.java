package nova.mjs.mentor.profile.dto;

import lombok.*;

import java.util.List;

// 검색 조건
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorSearchConditionDTO {
    private String q;                 // 키워드(회사/직무/설명)
    private List<String> skills;      // 스킬(ANY 매칭)
    private String sort;              // recent | popular | responseRate (엔티티 필드 부재 시 recent만 동작)
}
