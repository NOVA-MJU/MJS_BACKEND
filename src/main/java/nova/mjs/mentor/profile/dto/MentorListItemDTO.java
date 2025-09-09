package nova.mjs.mentor.profile.dto;

import lombok.*;
import nova.mjs.mentor.profile.entity.Mentor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorListItemDTO {
    private Long id;
    private String displayName;        // "김OO 선배"
    private String profileImageUrl;    // null 가능
    private String jobTitle;
    private String workplace;
    private Integer hiredYear;
    private List<String> skills;
    private String oneLiner;
    private String departmentName;     // member에서 가져옴
    private long mentoringCount;       // 목록/추천 공용
    private boolean featured;          // 추천 배지 여부
    private String ribbon;             // "FEATURED" 등(선택)
    private String achievement;        // "우수상"(선택)

    public static MentorListItemDTO from(Mentor m,
                                         long mentoringCount,
                                         String displayName,
                                         String departmentName,
                                         String profileImageUrl,
                                         boolean featured,
                                         String ribbon,
                                         String achievement) {
        return MentorListItemDTO.builder()
                .id(m.getId())
                .displayName(displayName)
                .profileImageUrl(profileImageUrl)
                .jobTitle(m.getJobTitle())
                .workplace(m.getWorkplace())
                .hiredYear(m.getHiredYear())    // mentor entity에 없음 아직
                .skills(m.getSkills())
                .oneLiner(shorten(m.getDescription()))
                .departmentName(departmentName)
                .mentoringCount(mentoringCount)
                .featured(featured)
                .ribbon(ribbon)
                .achievement(achievement)
                .build();
    }

    private static String shorten(String s) {
        return s == null ? "" : (s.length() > 90 ? s.substring(0, 90) + "..." : s);
    }
}