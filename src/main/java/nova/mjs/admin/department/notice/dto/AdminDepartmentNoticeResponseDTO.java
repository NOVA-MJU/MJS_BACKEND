package nova.mjs.admin.department.notice.dto;

import lombok.Builder;
import lombok.Data;
import nova.mjs.domain.department.entity.DepartmentNotice;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AdminDepartmentNoticeResponseDTO {
    private UUID uuid;
    private String title;
    private String content;
    private String thumbnailUrl;
    private LocalDateTime createAt;

    public static AdminDepartmentNoticeResponseDTO fromEntity(DepartmentNotice departmentNotice){
        return AdminDepartmentNoticeResponseDTO.builder()
                .uuid(departmentNotice.getUuid())
                .title(departmentNotice.getTitle())
                .content(departmentNotice.getContent())
                .thumbnailUrl(departmentNotice.getThumbnailUrl())
                .createAt(departmentNotice.getCreatedAt())
                .build();
    }
}