package nova.mjs.domain.thingo.department.service.notice;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentNotice;
import nova.mjs.domain.thingo.department.repository.DepartmentNoticeRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import nova.mjs.domain.thingo.department.service.notice.crawl.CrawledDepartmentNotice;
import nova.mjs.domain.thingo.department.service.notice.crawl.DepartmentNoticeListParser;
import nova.mjs.domain.thingo.department.service.notice.crawl.DepartmentNoticeSource;
import nova.mjs.domain.thingo.department.service.notice.crawl.DepartmentNoticeSourceRegistry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentNoticeCrawlingService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentNoticeRepository departmentNoticeRepository;
    private final List<DepartmentNoticeListParser> parsers;

    @Transactional
    public CrawlReport crawlAll() {
        List<SourceCrawlResult> results = new ArrayList<>();

        for (DepartmentNoticeSource source : DepartmentNoticeSourceRegistry.sources()) {
            try {
                results.add(crawlSingleSource(source));
            } catch (Exception e) {
                log.error("[MJS][DepartmentNotice] source={} 크롤링 실패", source.label(), e);
                results.add(SourceCrawlResult.fail(source.label(), source.url(), e.getMessage()));
            }
        }

        return CrawlReport.of(results);
    }

    private SourceCrawlResult crawlSingleSource(DepartmentNoticeSource source) throws IOException {
        Optional<Department> department = resolveDepartment(source);
        if (department.isEmpty()) {
            log.warn("[MJS][DepartmentNotice] source={} 에 해당하는 Department가 없어 스킵합니다.", source.label());
            return SourceCrawlResult.skip(source.label(), source.url(), "department 매핑 없음");
        }

        Document document = Jsoup.connect(source.url())
                .userAgent("Mozilla/5.0")
                .timeout(10_000)
                .get();

        ParseOutcome outcome = parseWithFallback(document, source);
        List<CrawledDepartmentNotice> crawled = outcome.items();

        if (crawled.isEmpty()) {
            log.warn("[MJS][DepartmentNotice] source={} 파싱 결과 0건 (parser={})", source.label(), outcome.parserName());
            return SourceCrawlResult.fail(source.label(), source.url(), "파싱 결과 0건");
        }

        List<DepartmentNotice> newNotices = new ArrayList<>();
        for (CrawledDepartmentNotice item : crawled) {
            if (departmentNoticeRepository.existsByDepartmentAndLink(department.get(), item.link())) {
                continue;
            }

            newNotices.add(DepartmentNotice.builder()
                    .departmentNoticeUuid(UUID.randomUUID())
                    .department(department.get())
                    .title(item.title())
                    .date(item.date())
                    .link(item.link())
                    .build());
        }

        if (!newNotices.isEmpty()) {
            departmentNoticeRepository.saveAll(newNotices);
        }

        log.info("[MJS][DepartmentNotice] source={} parser={} 파싱 {}건 / 신규 저장 {}건",
                source.label(), outcome.parserName(), crawled.size(), newNotices.size());

        return SourceCrawlResult.success(source.label(), source.url(), outcome.parserName(), crawled.size(), newNotices.size());
    }

    private ParseOutcome parseWithFallback(Document document, DepartmentNoticeSource source) {
        DepartmentNoticeListParser preferred = parsers.stream()
                .filter(p -> p.supports(source.sourceType()))
                .findFirst()
                .orElse(null);

        List<ParseOutcome> outcomes = new ArrayList<>();

        if (preferred != null) {
            List<CrawledDepartmentNotice> preferredItems = preferred.parse(document, source);
            outcomes.add(new ParseOutcome(preferred.getClass().getSimpleName(), preferredItems));
            if (!preferredItems.isEmpty()) {
                return outcomes.get(0);
            }
        }

        for (DepartmentNoticeListParser parser : parsers) {
            if (parser == preferred) {
                continue;
            }
            List<CrawledDepartmentNotice> parsed = parser.parse(document, source);
            outcomes.add(new ParseOutcome(parser.getClass().getSimpleName(), parsed));
        }

        return outcomes.stream()
                .max(Comparator.comparingInt(o -> o.items().size()))
                .orElse(new ParseOutcome("NONE", List.of()));
    }

    private Optional<Department> resolveDepartment(DepartmentNoticeSource source) {
        if (source.departmentName() == null) {
            return departmentRepository.findByCollegeAndDepartmentNameIsNull(source.college());
        }

        return departmentRepository.findByCollegeAndDepartmentName(source.college(), source.departmentName());
    }

    private record ParseOutcome(String parserName, List<CrawledDepartmentNotice> items) {
    }

    @Getter
    public static class CrawlReport {
        private final int total;
        private final int success;
        private final int failed;
        private final int skipped;
        private final List<SourceCrawlResult> results;

        private CrawlReport(int total, int success, int failed, int skipped, List<SourceCrawlResult> results) {
            this.total = total;
            this.success = success;
            this.failed = failed;
            this.skipped = skipped;
            this.results = results;
        }

        public static CrawlReport of(List<SourceCrawlResult> results) {
            int total = results.size();
            int success = (int) results.stream().filter(SourceCrawlResult::isSuccess).count();
            int skipped = (int) results.stream().filter(SourceCrawlResult::isSkipped).count();
            int failed = total - success - skipped;
            return new CrawlReport(total, success, failed, skipped, results);
        }
    }

    @Getter
    @Builder
    public static class SourceCrawlResult {
        private String sourceLabel;
        private String sourceUrl;
        private String parser;
        private int parsedCount;
        private int savedCount;
        private boolean success;
        private boolean skipped;
        private String message;

        public static SourceCrawlResult success(String label, String url, String parser, int parsedCount, int savedCount) {
            return SourceCrawlResult.builder()
                    .sourceLabel(label)
                    .sourceUrl(url)
                    .parser(parser)
                    .parsedCount(parsedCount)
                    .savedCount(savedCount)
                    .success(true)
                    .skipped(false)
                    .message("ok")
                    .build();
        }

        public static SourceCrawlResult fail(String label, String url, String message) {
            return SourceCrawlResult.builder()
                    .sourceLabel(label)
                    .sourceUrl(url)
                    .parser("unknown")
                    .parsedCount(0)
                    .savedCount(0)
                    .success(false)
                    .skipped(false)
                    .message(message)
                    .build();
        }

        public static SourceCrawlResult skip(String label, String url, String message) {
            return SourceCrawlResult.builder()
                    .sourceLabel(label)
                    .sourceUrl(url)
                    .parser("none")
                    .parsedCount(0)
                    .savedCount(0)
                    .success(false)
                    .skipped(true)
                    .message(message)
                    .build();
        }
    }
}
