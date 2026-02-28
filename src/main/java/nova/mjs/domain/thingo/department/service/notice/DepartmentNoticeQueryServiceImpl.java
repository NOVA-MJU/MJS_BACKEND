package nova.mjs.domain.thingo.department.service.notice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.department.dto.DepartmentNoticeDTO;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentNotice;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.exception.CollegeNotFoundException;
import nova.mjs.domain.thingo.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.thingo.department.repository.DepartmentNoticeRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentNoticeQueryServiceImpl implements DepartmentNoticeQueryService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentNoticeRepository noticeRepository;

    /* =========================================================
     * 조회 (기존)
     * ========================================================= */

    @Override
    public Page<DepartmentNoticeDTO.Summary> getDepartmentNoticePage(
            College college,
            DepartmentName departmentName,
            Pageable pageable
    ) {
        Department department = getDepartment(college, departmentName);

        return noticeRepository
                .findByDepartmentOrderByDateDesc(department, pageable)
                .map(DepartmentNoticeDTO.Summary::fromEntity);
    }

    /* =========================================================
     * 크롤링 (요구사항 반영: 전학과/단과 포함)
     * ========================================================= */

    @Override
    @Transactional
    public void crawlDepartmentNotices(College college, DepartmentName departmentName) {

        // 1) 전 단과대/전 학과 (단과 공지 포함)
        if (college == null && departmentName == null) {
            List<Department> targets = departmentRepository.findAll();
            crawlTargets(targets);
            return;
        }

        // 2) 특정 단과대 전체 (단과 공지 + 소속 학과 전체)
        if (college != null && departmentName == null) {
            List<Department> targets = departmentRepository.findByCollege(college);
            crawlTargets(targets);
            return;
        }

        // 3) 특정 학과
        Department department = getDepartment(college, departmentName);
        crawlOneDepartment(department);
    }

    /**
     * 다건 크롤링: 하나 실패해도 전체 중단하지 않음
     */
    private void crawlTargets(List<Department> targets) {
        for (Department d : targets) {
            try {
                crawlOneDepartment(d);
            } catch (Exception e) {
                log.warn("Department notice crawl failed. college={}, deptName={}, departmentId={}, reason={}",
                        d.getCollege(), d.getDepartmentName(), d.getId(), e.getMessage());
            }
        }
    }

    /**
     * 단일 Department(단과 또는 학과) 크롤링
     * - URL은 DepartmentNoticeUrlMap에서 (college, departmentName)로 찾고,
     *   없으면 (college, null)로 fallback
     * - 매핑이 없으면 skip 처리 (전체 크롤링 안정화)
     */
    private void crawlOneDepartment(Department department) {
        College college = department.getCollege();
        DepartmentName deptName = department.getDepartmentName(); // null이면 단과

        Optional<String> urlOpt = DepartmentNoticeUrlMap.get(college, deptName);
        if (urlOpt.isEmpty()) {
            log.info("공지 URL 매핑 없음 - skip. college={}, deptName={}, departmentId={}",
                    college, deptName, department.getId());
            return;
        }

        String listUrl = urlOpt.get();

        Document doc = fetch(listUrl);
        List<NoticeItem> items = extractNotices(doc, listUrl);

        if (items.isEmpty()) {
            throw new IllegalStateException("공지 파싱 결과 0건. url=" + listUrl);
        }

        saveNewNoticesOnly(department, items);
    }

    /* =========================================================
     * Department 조회 공통
     * ========================================================= */

    private Department getDepartment(College college, DepartmentName departmentName) {
        if (college == null) {
            throw new IllegalArgumentException("college는 특정 학과 크롤링/조회 시 필수입니다.");
        }

        if (departmentName == null) {
            return departmentRepository
                    .findCollegeLevelDepartment(college)
                    .orElseThrow(CollegeNotFoundException::new);
        }

        return departmentRepository
                .findByCollegeAndDepartmentName(college, departmentName)
                .orElseThrow(DepartmentNotFoundException::new);
    }

    /* =========================================================
     * Fetch
     * ========================================================= */

    private static final int TIMEOUT_MS = 10_000;

    private Document fetch(String url) {
        try {
            Connection conn = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; mjs-bot/1.0)")
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true);

            return conn.get();
        } catch (Exception e) {
            throw new IllegalStateException("페이지 fetch 실패: " + url, e);
        }
    }

    /* =========================================================
     * Parse (공통 파서)
     *
     * 시도 순서:
     *  1) MJU subview/do 계열 게시판(테이블/리스트)
     *  2) artclList.do 계열(사학과)
     *  3) default/php 계열: th 헤더(제목/등록/작성/날짜) 기반 테이블
     *  4) 블로그(article 기반)
     *  5) 최후: a + 날짜 패턴 휴리스틱
     * ========================================================= */

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(20\\d{2})[./-](0?\\d|1[0-2])[./-](0?\\d|[12]\\d|3[01])"
    );

    private List<NoticeItem> extractNotices(Document doc, String baseUrl) {
        List<NoticeItem> out;

        out = tryParseMjuBoardTableOrList(doc, baseUrl);
        if (!out.isEmpty()) return out;

        out = tryParseArtclList(doc, baseUrl);
        if (!out.isEmpty()) return out;

        out = tryParseByTableHeaders(doc, baseUrl);
        if (!out.isEmpty()) return out;

        out = tryParseBlogLike(doc, baseUrl);
        if (!out.isEmpty()) return out;

        return tryParseAnchorsWithDates(doc, baseUrl);
    }

    private List<NoticeItem> tryParseMjuBoardTableOrList(Document doc, String baseUrl) {
        Elements containers = doc.select(
                "#jwxe_main_content, .jwxe_main_content, #contents, #content, .contents, .sub_contents"
        );
        if (containers.isEmpty()) containers = new Elements(doc);

        Elements tables = new Elements();
        for (Element c : containers) {
            tables.addAll(c.select("table.board-table, table.bbs_list, table.tbl_board, table"));
        }

        for (Element table : tables) {
            if (table.select("a[href]").size() < 3) continue;

            List<NoticeItem> parsed = parseGenericTable(table, baseUrl);
            if (parsed.size() >= 3) return parsed;
        }

        Elements lists = doc.select(
                "ul.board-list, ul.bbs_list, ul.notice_list, .board_list ul, .bbs ul"
        );
        for (Element ul : lists) {
            List<NoticeItem> parsed = parseGenericList(ul, baseUrl);
            if (parsed.size() >= 3) return parsed;
        }

        return List.of();
    }

    private List<NoticeItem> tryParseArtclList(Document doc, String baseUrl) {
        for (Element table : doc.select("table")) {
            if (table.select("a[href]").size() < 3) continue;

            List<NoticeItem> parsed = parseGenericTable(table, baseUrl);
            if (parsed.size() >= 2) return parsed;
        }
        return List.of();
    }

    private List<NoticeItem> tryParseByTableHeaders(Document doc, String baseUrl) {
        for (Element table : doc.select("table")) {
            String headerText = table.select("th").text();
            if (headerText == null) headerText = "";

            boolean looksLikeNotice =
                    headerText.contains("제목") &&
                            (headerText.contains("등록")
                                    || headerText.contains("작성")
                                    || headerText.contains("날짜")
                                    || headerText.contains("일자"));

            if (!looksLikeNotice) continue;

            List<NoticeItem> parsed = parseGenericTable(table, baseUrl);
            if (parsed.size() >= 2) return parsed;
        }
        return List.of();
    }

    private List<NoticeItem> tryParseBlogLike(Document doc, String baseUrl) {
        Elements articles = doc.select("article");
        if (articles.isEmpty()) return List.of();

        List<NoticeItem> out = new ArrayList<>();
        for (Element a : articles) {
            Element titleA = a.selectFirst("h1 a[href], h2 a[href], h3 a[href], .entry-title a[href], .post-title a[href]");
            if (titleA == null) continue;

            String title = clean(titleA.text());
            String link = normalizeNoticeUrl(baseUrl, titleA.attr("href"));
            if (title.isBlank() || link == null) continue;

            LocalDate date = null;
            Element time = a.selectFirst("time[datetime], time");
            if (time != null) {
                date = parseDateLoose(time.attr("datetime"));
                if (date == null) date = parseDateLoose(time.text());
            }

            out.add(new NoticeItem(title, link, date));
        }

        return dedupAndLimit(out, 50);
    }

    private List<NoticeItem> tryParseAnchorsWithDates(Document doc, String baseUrl) {
        List<NoticeItem> out = new ArrayList<>();

        for (Element a : doc.select("a[href]")) {
            String title = clean(a.text());
            if (title.length() < 2) continue;

            String link = normalizeNoticeUrl(baseUrl, a.attr("href"));
            if (link == null) continue;

            if (isClearlyNotNoticeLink(link, title)) continue;

            String ctx = (a.parent() != null) ? a.parent().text() : a.text();
            LocalDate date = parseDateLoose(ctx);
            if (date == null) continue;

            out.add(new NoticeItem(title, link, date));
        }

        return dedupAndLimit(out, 50);
    }

    private List<NoticeItem> parseGenericTable(Element table, String baseUrl) {
        List<NoticeItem> out = new ArrayList<>();

        Elements rows = table.select("tbody tr");
        if (rows.isEmpty()) rows = table.select("tr");

        for (Element tr : rows) {
            if (!tr.select("th").isEmpty()) continue;

            Element a = tr.selectFirst("a[href]");
            if (a == null) continue;

            String title = clean(a.text());
            if (title.isBlank()) continue;

            String link = normalizeNoticeUrl(baseUrl, a.attr("href"));
            if (link == null) continue;

            if (isClearlyNotNoticeLink(link, title)) continue;

            LocalDate date = parseDateLoose(tr.text());
            out.add(new NoticeItem(title, link, date));
        }

        return dedupAndLimit(out, 50);
    }

    private List<NoticeItem> parseGenericList(Element ul, String baseUrl) {
        List<NoticeItem> out = new ArrayList<>();

        for (Element li : ul.select("li")) {
            Element a = li.selectFirst("a[href]");
            if (a == null) continue;

            String title = clean(a.text());
            if (title.isBlank()) continue;

            String link = normalizeNoticeUrl(baseUrl, a.attr("href"));
            if (link == null) continue;

            if (isClearlyNotNoticeLink(link, title)) continue;

            LocalDate date = parseDateLoose(li.text());
            out.add(new NoticeItem(title, link, date));
        }

        return dedupAndLimit(out, 50);
    }

    /* =========================================================
     * Save
     * ========================================================= */

    private void saveNewNoticesOnly(Department department, List<NoticeItem> items) {
        List<DepartmentNotice> toSave = new ArrayList<>();

        for (NoticeItem item : items) {
            if (noticeRepository.existsByDepartmentAndLink(department, item.link())) {
                continue;
            }

            LocalDateTime dateTime = (item.date() != null)
                    ? item.date().atStartOfDay()
                    : LocalDate.now().atStartOfDay();

            DepartmentNotice notice = DepartmentNotice.builder()
                    .departmentNoticeUuid(UUID.randomUUID())
                    .department(department)
                    .title(item.title())
                    .date(dateTime)
                    .link(item.link())
                    .build();

            toSave.add(notice);
        }

        if (!toSave.isEmpty()) {
            noticeRepository.saveAll(toSave);
        }
    }

    /* =========================================================
     * Utils
     * ========================================================= */

    private boolean isClearlyNotNoticeLink(String url, String title) {
        String u = url.toLowerCase(Locale.ROOT);
        if (u.startsWith("javascript:")) return true;
        if (u.contains("/login") || u.contains("logout")) return true;
        if (u.contains("#")) return true;
        if (title.contains("전체보기") || title.equalsIgnoreCase("more")) return true;
        return false;
    }

    private String clean(String s) {
        if (s == null) return "";
        return s.replace("\u00A0", " ").trim().replaceAll("\\s+", " ");
    }

    private LocalDate parseDateLoose(String text) {
        if (text == null) return null;
        Matcher m = DATE_PATTERN.matcher(text);
        if (!m.find()) return null;

        String y = m.group(1);
        String mm = String.format("%02d", Integer.parseInt(m.group(2)));
        String dd = String.format("%02d", Integer.parseInt(m.group(3)));

        return LocalDate.parse(y + "-" + mm + "-" + dd, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private List<NoticeItem> dedupAndLimit(List<NoticeItem> list, int limit) {
        LinkedHashMap<String, NoticeItem> map = new LinkedHashMap<>();
        for (NoticeItem i : list) map.putIfAbsent(i.link(), i);
        List<NoticeItem> out = new ArrayList<>(map.values());
        return out.size() > limit ? out.subList(0, limit) : out;
    }

    /* =========================================================
     * MJU enc(subview) 변환 지원
     * ========================================================= */

    private static final String SUBVIEW_SUFFIX = "/subview.do?enc=";

    /**
     * href를 절대 URL로 만든 뒤,
     * MJU 게시판의 artclView.do 링크라면 subview.do?enc= 로 변환한다.
     */
    private String normalizeNoticeUrl(String baseUrl, String href) {
        if (href == null || href.isBlank()) return null;

        // 1) 일단 절대 URL로 resolve
        final String resolved;
        try {
            resolved = URI.create(baseUrl).resolve(href).toString();
        } catch (Exception e) {
            return null;
        }

        // 2) 이미 subview(enc)면 그대로
        if (resolved.contains("/subview.do?enc=")) {
            return resolved;
        }

        // 3) artclView.do 계열이면 enc(subview)로 치환
        if (isArtclViewUrl(resolved)) {
            String subviewBase = buildSubviewBaseFrom(baseUrl);

            // encode 함수가 기대하는 "path + ?query" 형태로 맞춤
            try {
                URI u = URI.create(resolved);
                String rawLink = u.getRawPath();
                if (u.getRawQuery() != null && !u.getRawQuery().isBlank()) {
                    rawLink += "?" + u.getRawQuery();
                }
                return subviewBase + encodeArtclViewToEnc(rawLink);
            } catch (Exception e) {
                // 실패 시 원본 링크라도 반환 (완전 실패 방지)
                return resolved;
            }
        }

        return resolved;
    }

    private boolean isArtclViewUrl(String url) {
        if (url == null) return false;
        String u = url.toLowerCase(Locale.ROOT);
        return u.contains("/artclview.do");
    }

    /**
     * listUrl(=baseUrl) 기준으로 subview.do?enc= 베이스를 만든다.
     *
     * baseUrl 예:
     * - https://english.mju.ac.kr/english/6923/subview.do?enc=....
     * - https://www.mju.ac.kr/mjukr/255/subview.do?enc=....
     *
     * 반환:
     * - https://english.mju.ac.kr/english/6923/subview.do?enc=
     */
    private String buildSubviewBaseFrom(String baseUrl) {
        try {
            URI u = URI.create(baseUrl);
            String scheme = u.getScheme();
            String host = u.getHost();
            int port = u.getPort();

            String path = u.getPath();
            if (path == null) path = "";

            int idx = path.lastIndexOf("/subview.do");
            if (idx >= 0) {
                path = path.substring(0, idx);
            }

            String authority = (port > 0) ? host + ":" + port : host;
            return scheme + "://" + authority + path + SUBVIEW_SUFFIX;

        } catch (Exception e) {
            // 최후 fallback
            int q = baseUrl.indexOf("?");
            String base = (q >= 0) ? baseUrl.substring(0, q) : baseUrl;
            if (!base.endsWith("/subview.do")) {
                return base + "?enc=";
            }
            return base + "?enc=";
        }
    }

    /**
     * 공지 상세 페이지 enc 파라미터 생성 (NoticeCrawlingService와 동일 로직)
     */
    private String encodeArtclViewToEnc(String rawLink) {

        String path = rawLink.split("\\?")[0];
        if (!path.startsWith("/")) path = "/" + path;

        String query =
                "?page=1&srchColumn=&srchWrd=&bbsClSeq=&bbsOpenWrdSeq=" +
                        "&rgsBgndeStr=&rgsEnddeStr=&isViewMine=false&isView=true&password=";

        String full = "fnct1|@@|" + path + query;

        return URLEncoder.encode(
                Base64.getEncoder().encodeToString(full.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8
        );
    }

    private record NoticeItem(String title, String link, LocalDate date) {}
}