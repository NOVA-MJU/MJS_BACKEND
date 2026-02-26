package nova.mjs.domain.thingo.department.service.notice.crawl;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WordpressDepartmentNoticeParser extends AbstractDepartmentNoticeParser {

    @Override
    public boolean supports(DepartmentNoticeSourceType sourceType) {
        return sourceType == DepartmentNoticeSourceType.WORDPRESS;
    }

    @Override
    public List<CrawledDepartmentNotice> parse(Document document, DepartmentNoticeSource source) {
        Elements rows = document.select("article, .post, .blog-post");
        return parseRows(rows, source, "h1 a, h2 a, h3 a, .entry-title a, a", "h1 a, h2 a, h3 a, .entry-title a, a", "time, .entry-date, .posted-on");
    }
}
