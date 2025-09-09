package nova.mjs.mentor.profile.dto;

import lombok.*;
import nova.mjs.mentor.profile.entity.Mentor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorDetailDTO {
    // 헤더
    private Long id;
    private String displayName;
    private String profileImageUrl;
    private String jobTitle;
    private String workplace;
    private Integer hiredYear;
    private String region;
    private List<String> skills;
    private boolean verified;

    // 본문 섹션
    private String aboutMe;
    private List<CareerBlock> careers;
    private List<ProjectBlock> projects;
    private Tips tips;

    // 학력
    private Education education;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CareerBlock {
        private String company;
        private String jobPosition;
        private LocalDate startedAt;
        private LocalDate endedAt;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectBlock {
        private String title;
        private String link;
        private List<String> skills;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Tips {
        private String resume;
        private String interview;
        private String portfolio;
        private String networking;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Education {
        private String departmentName;
        private Integer graduationYear;
        private String schoolName;
    }

    public static MentorDetailDTO from(Mentor m,
                                       String displayName,
                                       String profileImageUrl,
                                       String departmentName,
                                       Integer graduationYear,
                                       String schoolName) {
        var careers = m.getCareers().stream().map(c -> CareerBlock.builder()
                .company(c.getCompany())
                .jobPosition(c.getJobPosition())
                .startedAt(c.getStartedAt())
                .endedAt(c.getEndedAt())
                .description(c.getDescription())
                .build()).toList();
        var projects = m.getPortfolios().stream().map(p -> ProjectBlock.builder()
                .title(p.getTitle())
                .link(p.getLink())
                .skills(p.getSkills())
                .description(p.getDescription())
                .build()).toList();
        var tips = new Tips(m.getResumeTips(), m.getInterviewTips(), m.getPortfolioTips(), m.getNetworkingTips());
        var edu = new Education(departmentName, graduationYear, schoolName);

        return MentorDetailDTO.builder()
                .id(m.getId())
                .displayName(displayName)
                .profileImageUrl(profileImageUrl)
                .jobTitle(m.getJobTitle())
                .workplace(m.getWorkplace())
                .hiredYear(m.getHiredYear())    // mentor entity에 없음 아직
                .region(m.getRegion())          // mentor entity에 없음 아직
                .skills(m.getSkills())
                .verified(m.isVerified())
                .aboutMe(m.getDescription())
                .careers(careers)
                .projects(projects)
                .tips(tips)
                .education(edu)
                .build();
    }
}