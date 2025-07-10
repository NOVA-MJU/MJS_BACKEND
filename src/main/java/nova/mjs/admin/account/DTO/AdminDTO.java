package nova.mjs.admin.account.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.admin.account.entity.Admin;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDTO {
    private String adminId;
    private String password;
    private String studentUnionName;
    private String department;
    private String logoImageUrl;
    private String instagramUrl;
    private String homepageUrl;
    private String introduction;
    private Admin.Role role;

    /**
     * Admin 엔티티를 AdminDTO 변환하는 메서드 (응답용)
     */
    public static AdminDTO fromEntity(Admin admin) {
        return AdminDTO.builder()
                .adminId(admin.getAdminId())
                .studentUnionName(admin.getStudentUnionName())
                .department(admin.getDepartment())
                .logoImageUrl(admin.getLogoImageUrl())
                .instagramUrl(admin.getInstagramUrl())
                .homepageUrl(admin.getHomepageUrl())
                .introduction(admin.getIntroduction())
                .role(admin.getRole())
                .build();
    }
    /**
     * 회원가입 요청을 위한 DTO (내부 클래스)
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminRequestDTO {
        private String adminId;
        private String password;
        private String studentUnionName;
        private String department;
        private String logoImageUrl;
        private String instagramUrl;
        private String homepageUrl;
        private String introduction;
    }

    /**
     * 비밀번호 변경 요청 DTO (내부 클래스)
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
