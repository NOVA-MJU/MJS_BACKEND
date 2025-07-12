package nova.mjs.admin.account.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import nova.mjs.admin.account.entity.Admin;
import nova.mjs.department.entity.enumList.College;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDTO {
    private String adminId;
    private String password;
    private String studentCouncilName;
    private String departmentName;
    private String slogan;
    private String logoImageUrl;
    private String instagramUrl;
    private String homepageUrl;
    private String introduction;
    private College college;
    private Admin.Role role;

    /**
     * 초기 관리자 등록
     */
    @Data
    public static class AdminIdRequestDTO{
        private String adminId;
    }

    /**
     * 사진 업로드 및 회원가입 데이터 저장 DTO
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminRequestDTO {
        @NotBlank(message = "학생회 이름은 비워둘 수 없습니다.")
        private String studentCouncilName;

        @NotBlank(message = "학과명은 비워둘 수 없습니다.")
        private String departmentName;

        private String slogan;
        private String instagramUrl;
        private String homepageUrl;
        private String introduction;
        private College college;
    }

    /**
     * 비밀번호 변경 요청 DTO
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PasswordRequestDTO {
        private String password;
        private String newPassword;
    }
}
