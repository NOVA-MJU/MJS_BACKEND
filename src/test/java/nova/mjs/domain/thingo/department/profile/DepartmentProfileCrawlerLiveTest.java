package nova.mjs.domain.thingo.department.profile;

import nova.mjs.domain.thingo.department.directory.DepartmentDirectoryCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;

/** 공식 사이트 변경 여부를 배포 전에 수동 검증하는 라이브 테스트. 기본 테스트에서는 실행하지 않는다. */
class DepartmentProfileCrawlerLiveTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "MJU_DEPARTMENT_PROFILE_LIVE", matches = "true")
    void crawlsEveryRegisteredOfficialHomepage() throws Exception {
        DepartmentDirectoryCatalog catalog = new DepartmentDirectoryCatalog();
        OfficialDepartmentProfileCrawler crawler = new OfficialDepartmentProfileCrawler();
        List<Callable<String>> jobs = catalog.entries().stream()
                .map(entry -> (Callable<String>) () -> {
                    OfficialDepartmentProfileCrawler.CrawlResult result = crawler.crawl(entry);
                    return String.join("|",
                            entry.displayName(), result.status(), result.message(),
                            String.valueOf(result.introductionSourceUrl()),
                            String.valueOf(result.majorCurriculumSourceUrl()));
                })
                .toList();

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<String> results = executor.invokeAll(jobs).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .toList();
            results.forEach(System.out::println);
            long complete = results.stream().filter(value -> value.contains("|COMPLETE|")).count();
            long partial = results.stream().filter(value -> value.contains("|PARTIAL|")).count();
            long unavailable = results.stream().filter(value -> value.contains("|UNAVAILABLE|")).count();
            long failed = results.stream().filter(value -> value.contains("|FAILED|")).count();
            System.out.printf("SUMMARY total=%d complete=%d partial=%d unavailable=%d failed=%d%n",
                    results.size(), complete, partial, unavailable, failed);
            assertThat(results).hasSize(catalog.entries().size());
        } finally {
            executor.shutdownNow();
        }
    }
}
