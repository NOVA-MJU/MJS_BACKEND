package nova.mjs.domain.thingo.department.profile;

import nova.mjs.domain.thingo.department.directory.DepartmentDirectoryCatalog;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OfficialDepartmentProfileCrawler {

    private static final int TIMEOUT_MS = 12_000;
    private static final int MAX_TEXT_LENGTH = 8_000;
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final List<String> INTRO_LABELS = List.of(
            "전공소개 및 특성", "학과소개", "전공소개", "학부소개", "대학소개");
    private static final List<String> GOAL_LABELS = List.of(
            "교육목표", "인재상", "학과 교육목표", "전공 교육목표");
    private static final List<String> CAREER_LABELS = List.of(
            "졸업 후 진로", "졸업후 진로", "진로 및 전망", "진로와 전망", "취업 및 진로");
    private static final List<String> CURRICULUM_LABELS = List.of(
            "교과과정", "교육과정", "전공과목", "교과목");
    private final ConcurrentHashMap<String, CachedDocument> pageCache = new ConcurrentHashMap<>();

    public CrawlResult crawl(DepartmentDirectoryCatalog.Entry entry) {
        if (entry.homepageUrl() == null) {
            return CrawlResult.unavailable("공식 홈페이지 URL 미등록");
        }

        try {
            validateOfficialUrl(entry.homepageUrl());
            Document home = fetch(entry.homepageUrl());
            Document landing = resolveDepartmentLanding(home, entry);

            PageContent introduction = fetchSection(landing, home, INTRO_LABELS, true);
            PageContent goals = fetchSection(landing, home, GOAL_LABELS, false);
            PageContent careers = fetchSection(landing, home, CAREER_LABELS, false);
            PageContent curriculum = fetchSection(landing, home, CURRICULUM_LABELS, false);

            int found = countPresent(introduction, goals, careers, curriculum);
            String status = found >= 3 ? "COMPLETE" : found > 0 ? "PARTIAL" : "FAILED";
            String message = "공식 페이지 항목 " + found + "/4 수집";
            return new CrawlResult(
                    introduction.text(), goals.text(), careers.text(), curriculum.text(),
                    introduction.url(), goals.url(), careers.url(), curriculum.url(),
                    status, message, LocalDateTime.now());
        } catch (Exception e) {
            return CrawlResult.failed(e.getMessage());
        }
    }

    private Document resolveDepartmentLanding(
            Document home,
            DepartmentDirectoryCatalog.Entry entry
    ) {
        if (entry.departmentLabel() == null) return home;
        Element departmentLink = findLink(home, List.of(entry.departmentLabel()));
        if (departmentLink == null) return home;
        String url = absoluteUrl(departmentLink);
        if (url == null) return home;
        try {
            return fetch(url);
        } catch (IllegalStateException ignored) {
            // 공식 메인 페이지의 오래된 외부 학과 링크가 깨져도 상위 공식 소개는 수집한다.
            return home;
        }
    }

    private PageContent fetchSection(
            Document landing,
            Document home,
            List<String> labels,
            boolean allowLandingFallback
    ) {
        if (isDetailPage(landing.location()) && hasContentLabel(landing, labels)) {
            String text = extractContent(landing, labels);
            if (!text.isBlank()) return new PageContent(text, landing.location());
        }

        Element link = findLink(landing, labels);
        if (link == null && landing != home) link = findLink(home, labels);

        if (link != null) {
            String url = absoluteUrl(link);
            if (url != null) {
                Document page = sameDocument(url, landing.location()) ? landing : fetch(url);
                String text = extractContent(page, labels);
                if (!text.isBlank()) return new PageContent(text, page.location());
            }
        }

        if (allowLandingFallback || hasContentLabel(landing, labels)) {
            String text = extractContent(landing, labels);
            if (!text.isBlank()) return new PageContent(text, landing.location());
        }
        return PageContent.empty();
    }

    private Element findLink(Document document, List<String> labels) {
        Element best = null;
        int bestScore = Integer.MIN_VALUE;
        for (Element link : document.select("a[href]")) {
            String href = link.attr("href").trim();
            if (href.isBlank() || href.equals("#") || href.toLowerCase().startsWith("javascript:")) {
                continue;
            }
            String text = DepartmentDirectoryCatalog.normalize(link.text());
            if (text.isBlank()) continue;
            for (String label : labels) {
                String normalizedLabel = DepartmentDirectoryCatalog.normalize(label);
                if (!text.contains(normalizedLabel)) continue;
                int score = text.equals(normalizedLabel) ? 1000 : 500;
                score -= text.length();
                if (score > bestScore) {
                    best = link;
                    bestScore = score;
                }
            }
        }
        return best;
    }

    private String extractContent(Document document, List<String> labels) {
        Element scope = bestContentScope(document, labels);
        Set<String> paragraphs = new LinkedHashSet<>();
        for (Element element : scope.select("p, li, dt, dd, th, td")) {
            String text = clean(element.text());
            if (isUseful(text)) paragraphs.add(text);
        }

        String result = String.join("\n", paragraphs);
        if (result.isBlank()) result = clean(scope.text());
        return truncate(result);
    }

    private Element bestContentScope(Document document, List<String> labels) {
        List<Element> candidates = new ArrayList<>();
        candidates.addAll(document.select("article, main, #content, #contents, .contents, .sub-content, .content"));
        candidates.add(document.body());

        Element best = document.body();
        int bestScore = Integer.MIN_VALUE;
        for (Element candidate : candidates) {
            if (candidate == null) continue;
            String text = candidate.text();
            int score = Math.min(text.length(), 20_000);
            if (containsAny(text, labels)) score += 50_000;
            if (candidate == document.body()) score -= 10_000;
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    private boolean containsAny(String text, List<String> labels) {
        String normalizedText = DepartmentDirectoryCatalog.normalize(text);
        return labels.stream()
                .map(DepartmentDirectoryCatalog::normalize)
                .anyMatch(normalizedText::contains);
    }

    private boolean hasContentLabel(Document document, List<String> labels) {
        return document.select("article, main, #content, #contents, .contents, .sub-content, .content")
                .stream()
                .anyMatch(element -> containsAny(element.text(), labels));
    }

    private boolean isDetailPage(String url) {
        String withoutQuery = url.replaceAll("[?#].*$", "").toLowerCase();
        return withoutQuery.endsWith("subview.do")
                || (withoutQuery.endsWith(".php") && !withoutQuery.endsWith("index.php"));
    }

    private boolean isUseful(String text) {
        if (text.length() < 12) return false;
        String normalized = DepartmentDirectoryCatalog.normalize(text);
        return !normalized.matches("(홈|home|로그인|사이트맵|개인정보처리방침|이메일수집거부)+");
    }

    private String clean(String value) {
        return value == null ? "" : value.replace('\u00a0', ' ')
                .replaceAll("[\\t\\r]+", " ")
                .replaceAll(" {2,}", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private String truncate(String text) {
        if (text.length() <= MAX_TEXT_LENGTH) return text;
        return text.substring(0, MAX_TEXT_LENGTH).trim();
    }

    private int countPresent(PageContent... contents) {
        int count = 0;
        for (PageContent content : contents) if (content.text() != null) count++;
        return count;
    }

    private String absoluteUrl(Element link) {
        String url = link.absUrl("href");
        if (url == null || url.isBlank() || url.startsWith("javascript:")) return null;
        validateOfficialUrl(url);
        return url;
    }

    private boolean sameDocument(String left, String right) {
        return left.replaceAll("#.*$", "").equals(right.replaceAll("#.*$", ""));
    }

    private Document fetch(String url) {
        validateOfficialUrl(url);
        CachedDocument cached = pageCache.get(url);
        if (cached != null && cached.isFresh()) return cached.document().clone();
        try {
            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; MJU-NOVA-DepartmentProfileBot/1.0)")
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(false)
                    .maxBodySize(2_000_000);
            Document document = connection.get();
            if (pageCache.size() >= 200) pageCache.clear();
            pageCache.put(url, new CachedDocument(document, Instant.now()));
            return document.clone();
        } catch (Exception e) {
            throw new IllegalStateException("공식 페이지 조회 실패: " + url, e);
        }
    }

    private record CachedDocument(Document document, Instant fetchedAt) {
        private boolean isFresh() {
            return fetchedAt.plus(CACHE_TTL).isAfter(Instant.now());
        }
    }

    private void validateOfficialUrl(String url) {
        URI uri = URI.create(url);
        String host = uri.getHost();
        boolean allowedScheme = "https".equalsIgnoreCase(uri.getScheme())
                || "http".equalsIgnoreCase(uri.getScheme());
        boolean allowedHost = host != null && (host.equals("mju.ac.kr")
                || host.endsWith(".mju.ac.kr")
                || host.equals("mjuecon.org")
                || host.endsWith(".mjuecon.org"));
        if (!allowedScheme || !allowedHost) {
            throw new IllegalArgumentException("허용되지 않은 학과 홈페이지 URL: " + url);
        }
    }

    private record PageContent(String text, String url) {
        private static PageContent empty() {
            return new PageContent(null, null);
        }
    }

    public record CrawlResult(
            String introduction,
            String educationalGoals,
            String careerPaths,
            String majorCurriculum,
            String introductionSourceUrl,
            String educationalGoalsSourceUrl,
            String careerPathsSourceUrl,
            String majorCurriculumSourceUrl,
            String status,
            String message,
            LocalDateTime verifiedAt
    ) {
        private static CrawlResult unavailable(String message) {
            return new CrawlResult(null, null, null, null, null, null, null, null,
                    "UNAVAILABLE", message, LocalDateTime.now());
        }

        private static CrawlResult failed(String message) {
            return new CrawlResult(null, null, null, null, null, null, null, null,
                    "FAILED", message, LocalDateTime.now());
        }
    }
}
