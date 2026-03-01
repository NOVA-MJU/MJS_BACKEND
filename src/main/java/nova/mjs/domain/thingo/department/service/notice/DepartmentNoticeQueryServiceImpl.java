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
     * 조회
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
     * 크롤링
     *
     * 정책:
     *  - (null, null) : 전체 = 모든 단과대 + 모든 학과
     *  - (college, null) : 단과대만(소속 학과 X)
     *  - (college, dept) : 해당 학과만
     *  - (null, dept) : 금지
     * ========================================================= */

    @Override
    @Transactional
    public void crawlDepartmentNotices(College college, DepartmentName departmentName) {

        // department 단독 요청 금지
        if (college == null && departmentName != null) {
            throw new IllegalArgumentException("department 단독 요청은 허용되지 않습니다. college와 함께 주세요.");
        }

        // 1) 전체: 모든 단과대 + 모든 학과
        if (college == null && departmentName == null) {
            crawlAllCollegeLevel();     // 단과대 레벨 공지 전부
            crawlAllDepartmentLevel();  // 학과 레벨 공지 전부
            return;
        }

        // 2) 단과대만: 단과대 레벨 Department 1개만
        if (college != null && departmentName == null) {
            Department collegeLevel = departmentRepository
                    .findCollegeLevelDepartment(college)
                    .orElseThrow(CollegeNotFoundException::new);
            crawlOneDepartment(collegeLevel);
            return;
        }

        // 3) 학과만: 해당 학과 Department 1개만
        Department department = getDepartment(college, departmentName);
        crawlOneDepartment(department);
    }

    private void crawlAllCollegeLevel() {
        for (College c : College.values()) {
            try {
                Department collegeLevel = departmentRepository
                        .findCollegeLevelDepartment(c)
                        .orElse(null);

                // DB에 단과대 레벨 레코드가 없는 college는 skip
                if (collegeLevel == null) continue;

                crawlOneDepartment(collegeLevel);
            } catch (Exception e) {
                log.warn("College-level notice crawl failed. college={}, reason={}", c, e.getMessage());
            }
        }
    }

    private void crawlAllDepartmentLevel() {
        List<Department> targets = departmentRepository.findAllByDepartmentNameIsNotNull();
        crawlTargets(targets);
    }

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

    private void crawlOneDepartment(Department department) {
        College college = department.getCollege();
        DepartmentName deptName = department.getDepartmentName();

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

        // 단과대 레벨
        if (departmentName == null) {
            return departmentRepository
                    .findCollegeLevelDepartment(college)
                    .orElseThrow(CollegeNotFoundException::new);
        }

        // 학과 레벨
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
     * Parse
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
     * MJU enc(subview) 변환 (핵심 수정)
     * - subview 베이스는 "목록 URL(DepartmentNoticeUrlMap)"에서만 가져온다.
     * ========================================================= */

    private String normalizeNoticeUrl(String baseUrl, String href) {
        if (href == null || href.isBlank()) return null;

        final String resolved;
        try {
            resolved = URI.create(baseUrl).resolve(href).toString();
        } catch (Exception e) {
            return null;
        }

        if (resolved.contains("/subview.do?enc=")) {
            return resolved;
        }

        if (isArtclViewUrl(resolved)) {
            try {
                String subviewBase = buildSubviewBaseFromListUrl(baseUrl);

                URI u = URI.create(resolved);
                String rawLink = u.getRawPath();
                if (u.getRawQuery() != null && !u.getRawQuery().isBlank()) {
                    rawLink += "?" + u.getRawQuery();
                }

                return subviewBase + encodeArtclViewToEnc(rawLink);
            } catch (Exception e) {
                return resolved;
            }
        }

        return resolved;
    }

    private boolean isArtclViewUrl(String url) {
        if (url == null) return false;
        return url.toLowerCase(Locale.ROOT).contains("/artclview.do");
    }

    private String buildSubviewBaseFromListUrl(String listUrl) {
        int idx = listUrl.indexOf("/subview.do");
        if (idx < 0) {
            throw new IllegalArgumentException("listUrl is not subview.do: " + listUrl);
        }
        return listUrl.substring(0, idx) + "/subview.do?enc=";
    }

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

    /* =========================================================
     * 삭제
     *
     * 정책을 크롤링과 동일하게 맞춤:
     *  - (null, null, null) : 전체 삭제
     *  - (college, null, null) : 단과대 레벨만 삭제(소속 학과 X)
     *  - (college, dept, null) : 해당 학과만 삭제
     *  - noticeUuid 존재 시: 단일 삭제(+ optional 검증)
     * ========================================================= */

    @Override
    @Transactional
    public void deleteDepartmentNotices(College college, DepartmentName departmentName, UUID noticeUuid) {

        if (college == null && departmentName != null) {
            throw new IllegalArgumentException("department 단독 요청은 허용되지 않습니다. college와 함께 주세요.");
        }

        // 1) 단일 삭제
        if (noticeUuid != null) {
            deleteOneNoticeWithOptionalValidation(college, departmentName, noticeUuid);
            return;
        }

        // 2) 전체 삭제
        if (college == null && departmentName == null) {
            noticeRepository.deleteAllInBatch();
            return;
        }

        // 3) 단과대만 삭제(단과대 레벨 Department 1개만)
        if (college != null && departmentName == null) {
            Department collegeLevel = departmentRepository
                    .findCollegeLevelDepartment(college)
                    .orElseThrow(CollegeNotFoundException::new);

            noticeRepository.deleteByDepartment(collegeLevel);
            return;
        }

        // 4) 학과만 삭제
        Department dept = getDepartment(college, departmentName);
        noticeRepository.deleteByDepartment(dept);
    }

    private void deleteOneNoticeWithOptionalValidation(
            College college,
            DepartmentName departmentName,
            UUID noticeUuid
    ) {
        DepartmentNotice notice = noticeRepository.findByDepartmentNoticeUuid(noticeUuid)
                .orElseThrow(() -> new NoSuchElementException("공지 없음: " + noticeUuid));

        // 옵션 검증: college가 들어오면 해당 notice의 department와 매칭되는지 확인
        if (college != null) {
            Department expected = getDepartment(college, departmentName);
            if (!notice.getDepartment().getId().equals(expected.getId())) {
                throw new IllegalArgumentException("공지-학과 정보가 일치하지 않습니다.");
            }
        }

        noticeRepository.delete(notice);
    }
}