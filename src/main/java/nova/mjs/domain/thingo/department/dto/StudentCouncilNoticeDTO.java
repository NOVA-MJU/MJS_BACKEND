package nova.mjs.domain.thingo.department.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.department.entity.StudentCouncilNotice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 학과 공지 관련 DTO 모음 컨테이너.
 *
 * <p>외부에 노출되는 실질 DTO는 내부 static 클래스를 통해 제공됩니다.</p>
 */
public final class StudentCouncilNoticeDTO {

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static class Summary {

        private final UUID noticeUuid;
        private final String title;
        private final String previewContent;
        private final String thumbnailUrl;
        private final LocalDateTime createdAt;

        /* ---- 변환 메서드 ---- */
        public static Summary fromEntity(StudentCouncilNotice n) {
            return Summary.builder()
                    .noticeUuid(n.getUuid())
                    .title(n.getTitle())
                    .previewContent(n.getPreviewContent())
                    .thumbnailUrl(n.getThumbnailUrl())
                    .createdAt(n.getCreatedAt())
                    .build();
        }

        public static List<Summary> fromList(List<StudentCouncilNotice> entities) {
            return entities.stream()
                    .map(Summary::fromEntity)
                    .toList();
        }
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static class Detail {

        private final UUID noticeUuid;
        private final String title;
        private final String content;
        private final String thumbnailUrl;
        private final LocalDateTime createdAt;

        public static Detail fromEntity(StudentCouncilNotice n) {
            return Detail.builder()
                    .noticeUuid(n.getUuid())
                    .title(n.getTitle())
                    .content(n.getContent())
                    .thumbnailUrl(n.getThumbnailUrl())
                    .createdAt(n.getCreatedAt())
                    .build();
        }
    }
}
