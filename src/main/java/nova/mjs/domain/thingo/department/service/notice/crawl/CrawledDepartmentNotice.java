package nova.mjs.domain.thingo.department.service.notice.crawl;

import java.time.LocalDateTime;

public record CrawledDepartmentNotice(
        String title,
        LocalDateTime date,
        String link
) {
}
