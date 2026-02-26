package nova.mjs.domain.thingo.department.service.notice.crawl;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GnuboardDepartmentNoticeParser extends AbstractDepartmentNoticeParser {

    @Override
    public boolean supports(DepartmentNoticeSourceType sourceType) {
        return sourceType == DepartmentNoticeSourceType.GNUBOARD || sourceType == DepartmentNoticeSourceType.PHP_BOARD;
    }

    @Override
    public List<CrawledDepartmentNotice> parse(Document document, DepartmentNoticeSource source) {
        Elements rows = document.select("table tbody tr, .board-list tbody tr");
        return parseRows(rows, source, "td.subject a, td.td_subject a, a", "td.subject a, td.td_subject a, a", "td.datetime, td.date, td:nth-last-child(2)");
    }
}
