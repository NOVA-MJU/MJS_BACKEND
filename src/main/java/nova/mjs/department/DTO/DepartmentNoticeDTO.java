// src/main/java/nova/mjs/department/DTO/DepartmentNoticeDTO.java
package nova.mjs.department.DTO;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.department.entity.DepartmentNotice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class DepartmentNoticeDTO {
    private UUID uuid;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    public static DepartmentNoticeDTO of(DepartmentNotice n) {
        return DepartmentNoticeDTO.builder()
                .uuid(n.getUuid())
                .title(n.getTitle())
                .content(n.getContent())
                // BaseEntity에 선언된 필드를 그대로 따라야 합니다.
                // 보통은 getCreatedAt() 이므로 아래처럼 수정하세요.
                .createdAt(n.getCreatedAt())
                .build();
    }
}
