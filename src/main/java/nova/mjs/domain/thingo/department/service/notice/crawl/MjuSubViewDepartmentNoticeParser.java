package nova.mjs.domain.thingo.department.service.notice.crawl;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MjuSubViewDepartmentNoticeParser extends AbstractDepartmentNoticeParser {

    @Override
    public boolean supports(DepartmentNoticeSourceType sourceType) {
        return sourceType == DepartmentNoticeSourceType.MJU_SUBVIEW;
    }

    @Override
    public List<CrawledDepartmentNotice> parse(Document document, DepartmentNoticeSource source) {
        Elements rows = document.select(".artclTable tbody tr");
        if (rows.isEmpty()) {
            rows = document.select("table tbody tr");
        }

        return parseRows(rows, source, ".artclLinkView, td.title a, a", ".artclLinkView, td.title a, a", "._artclTdRdate, td.date, td:nth-last-child(1)");
    }
}
