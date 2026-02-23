package nova.mjs.domain.thingo.department.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.department.entity.DepartmentNotice;

import java.time.LocalDateTime;
import java.util.UUID;

public class DepartmentNoticeDTO {

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static class Summary {

        private final UUID departmentNoticeUuid;
        private final String title;
        private final LocalDateTime date;
        private final String link;

        public static DepartmentNoticeDTO.Summary fromEntity(DepartmentNotice n) {
            return Summary.builder()
                    .departmentNoticeUuid(n.getDepartmentNoticeUuid())
                    .title(n.getTitle())
                    .date(n.getDate())
                    .link(n.getLink())
                    .build();
        }
    }
}
