package nova.mjs.domain.thingo.department.service.notice.crawl;

import org.jsoup.nodes.Document;

import java.util.List;

public interface DepartmentNoticeListParser {

    boolean supports(DepartmentNoticeSourceType sourceType);

    List<CrawledDepartmentNotice> parse(Document document, DepartmentNoticeSource source);
}
