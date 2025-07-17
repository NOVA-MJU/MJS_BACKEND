package nova.mjs.admin.department.department_notice.dto;

import lombok.Builder;
import lombok.Data;
import nova.mjs.domain.department.entity.DepartmentNotice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AdminDepartmentNoticeResponseDTO {
    private UUID uuid;
    private String title;
    private String content;
    private LocalDateTime createAt;

    public static AdminDepartmentNoticeResponseDTO fromEntity(DepartmentNotice n){
        return AdminDepartmentNoticeResponseDTO.builder()
                .uuid(n.getDepartmentNoticeUuid())
                .title(n.getTitle())
                .content(n.getContent())
                .createAt(n.getCreatedAt())
                .build();
    }
}