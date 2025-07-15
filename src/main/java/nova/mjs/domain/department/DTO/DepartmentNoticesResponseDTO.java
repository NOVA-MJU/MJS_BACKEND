package nova.mjs.domain.department.DTO;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.department.entity.DepartmentNotice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class DepartmentNoticesResponseDTO {
    private DepartmentInfoDTO departmentInfo;
    private List<NoticeSimpleDTO> notices;

    public static DepartmentNoticesResponseDTO fromNoticeList(Department department, List<DepartmentNotice> noticeEntities) {
        return DepartmentNoticesResponseDTO.builder()
                .departmentInfo(DepartmentInfoDTO.fromDepartmentEntity(department))
                .notices(NoticeSimpleDTO.fromList(noticeEntities))
                .build();
    }

    @Getter
    @Builder
    public static class NoticeSimpleDTO {
        private UUID departmentNoticeUuid;
        private String title;
        private String previewContent;
        private String thumbnailUrl;
        private LocalDateTime createdAt;

        public static NoticeSimpleDTO fromNoticeEntityPreview(DepartmentNotice n) {
            return NoticeSimpleDTO.builder()
                    .departmentNoticeUuid(n.getDepartmentNoticeUuid())
                    .title(n.getTitle())
                    .previewContent(n.getPreviewContent())
                    .thumbnailUrl(n.getThumbnailUrl())
                    .createdAt(n.getCreatedAt())
                    .build();
        }

        public static List<NoticeSimpleDTO> fromList(List<DepartmentNotice> list) {
            return list.stream()
                    .map(NoticeSimpleDTO::fromNoticeEntityPreview)
                    .toList();
        }
    }

    @Getter
    @Builder
    public static class DepartmentNoticeDetailDTO {
        private UUID departmentNoticeUuid;
        private String title;
        private String content;          // 전체 content
        private String thumbnailUrl;
        private LocalDateTime createdAt;

        public static DepartmentNoticeDetailDTO of(
                nova.mjs.domain.department.entity.DepartmentNotice n
        ) {
            return DepartmentNoticeDetailDTO.builder()
                    .departmentNoticeUuid(n.getDepartmentNoticeUuid())
                    .title(n.getTitle())
                    .content(n.getContent())
                    .thumbnailUrl(n.getThumbnailUrl())
                    .createdAt(n.getCreatedAt())
                    .build();
        }
    }
}
