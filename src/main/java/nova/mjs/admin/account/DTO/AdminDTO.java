package nova.mjs.admin.account.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.entity.enumList.College;
import nova.mjs.domain.member.entity.enumList.DepartmentName;

public class AdminDTO {
    /**
     * 학생홰 회원가입 요청을 위한 DTO (내부 클래스)
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentCouncilInitRegistrationRequestDTO {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email; // 이메일 아이디

        @NotBlank(message = "이름은 필수입니다.")
        private String name;

        @NotBlank(message = "컨텍 이메일은 필수입니다.")
        private String contactEmail;

        @NotNull(message = "담당 학과를 입력해주세요")
        private DepartmentName departmentName;

        @Builder.Default
        private String password = "hellomjs1!";
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentCouncilUpdateDTO {
        @NotBlank(message = "이메일은 검증은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email; // 이메일 아이디

        private String name;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;

        @NotNull(message = "학과 대학는 필수입니다.")
        private College college;

        private DepartmentName departmentName;

        private String profileImageUrl;

        private String slogan;

        private String description;

        private String instagramUrl;

        private String homepageUrl;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentCouncilResponseDTO {
        private String adminEmail;             // 로그인용 관리자 이메일 (Member)
        private String name;                   // 관리자 이름 (ex. 학생회명)
        private String studentCouncilEmail;    // 공식 연락 이메일
        private College college;               // 소속 대학
        private DepartmentName departmentName; // 소속 학과
        private String profileImageUrl;        // 관리자 프로필 이미지 (학생회 로고)
        private String slogan;                 // 슬로건
        private String description;            // 학생회 소개
        private String instagramUrl;           // 인스타그램 링크
        private String homepageUrl;            // 공식 홈페이지 링크


        public static AdminDTO.StudentCouncilResponseDTO fromEntity(Member member, Department department) {
            return AdminDTO.StudentCouncilResponseDTO.builder()
                    .adminEmail(member.getEmail())
                    .name(member.getName())
                    .studentCouncilEmail(department.getStudentCouncilContactEmail())
                    .college(department.getCollege())
                    .departmentName(department.getDepartmentName())
                    .profileImageUrl(member.getProfileImageUrl())
                    .slogan(department.getSlogan())
                    .description(department.getDescription())
                    .instagramUrl(department.getInstagramUrl())
                    .homepageUrl(department.getHomepageUrl())
                    .build();
        }
    }
}
