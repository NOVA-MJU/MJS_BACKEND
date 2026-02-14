package nova.mjs.domain.thingo.department.dto;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;

public class DepartmentDTO {

    /* ==================================================
     * 생성 요청 DTO
     * ================================================== */
    @Getter
    public static class CreateRequest {

        private College college;
        private DepartmentName departmentName;
        private String academicOfficePhone;
        private String instagramUrl;
        private String homepageUrl;
    }

    /* ==================================================
     * 수정 요청 DTO
     * ================================================== */
    @Getter
    public static class UpdateRequest {
        private College college;
        private DepartmentName departmentName;
        private String academicOfficePhone;
        private String instagramUrl;
        private String homepageUrl;
    }

    /* ==================================================
     * 정보 조회 응답 DTO (단건)
     * ================================================== */
    @Getter
    @Builder
    public static class InfoResponse {

        private College college;
        private DepartmentName departmentName;
        private String academicOfficePhone;
        private String instagramUrl;
        private String homepageUrl;

        public static InfoResponse fromEntity(Department department) {
            return InfoResponse.builder()
                    .college(department.getCollege())
                    .departmentName(department.getDepartmentName())
                    .academicOfficePhone(department.getAcademicOfficePhone())
                    .instagramUrl(department.getInstagramUrl())
                    .homepageUrl(department.getHomepageUrl())
                    .build();
        }
    }
}
