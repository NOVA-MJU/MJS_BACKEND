package nova.mjs.domain.thingo.notice.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.notice.entity.Notice;
import nova.mjs.domain.thingo.notice.repository.NoticeRepository;
import nova.mjs.domain.thingo.notice.service.crawl.NoticeCrawlHelper;
import nova.mjs.domain.thingo.notice.service.crawl.NoticeUrlRegistry;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 조회: 크롤링은 트랜잭션 밖에서 수행하고, 저장만 별도 트랜잭션으로 수행한다.
public class NoticeCrawlingService {

    private final NoticeRepository noticeRepository;
    private final ApplicationContext applicationContext;

    /*
     * 상세 페이지 URL 조립 규칙의 일부.
     * Registry는 "무엇을 크롤링할지(카테고리 -> 목록 path)"만 관리하고,
     * Service는 "어떤 규칙으로 상세 URL을 만들지"를 관리한다.
     */
    private static final String SUBVIEW_BASE =
            "https://www.mju.ac.kr/mjukr/255/subview.do?enc=";

    /**
     * 모든 공지 크롤링 진입점.
     *
     * 설계 의도:
     * 1) 스레드 점유 시간을 최소화하기 위해 "카테고리 단위"로 작업을 쪼갠다.
     * 2) 어떤 카테고리가 실패하더라도 다른 카테고리는 계속 진행하도록 예외를 격리한다.
     * 3) 트랜잭션은 DB 저장 시점에만 짧게 잡는다.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void fetchAllNotices() {
        // 학교 공지 크롤링
        crawlGroup(NoticeUrlRegistry.schoolNoticeUrls());
        // 학과 공지 크롤링
        crawlGroup(NoticeUrlRegistry.departmentNoticeUrls());
    }

    /**
     * 특정 카테고리(type)만 크롤링
     *
     * @param type 공지 카테고리 (ex: general, academic, law)
     */
    public void fetchNoticesByType(String type) {

        // 1. 학교 공지에서 먼저 찾기
        if (NoticeUrlRegistry.schoolNoticeUrls().containsKey(type)) {
            crawlSingleCategory(type, NoticeUrlRegistry.schoolNoticeUrls().get(type));
            return;
        }

        // 2. 학과 공지에서 찾기
        if (NoticeUrlRegistry.departmentNoticeUrls().containsKey(type)) {
            crawlSingleCategory(type, NoticeUrlRegistry.departmentNoticeUrls().get(type));
            return;
        }

        // 3. 어디에도 없으면 잘못된 요청
        throw new IllegalArgumentException("지원하지 않는 공지 type입니다: " + type);
    }

    /**
     * 공지 그룹 단위 크롤링.
     *
     * 실패 격리 정책:
     * - group 전체를 try-catch로 감싸면, 특정 학과/카테고리의 실패가 group 전체 실패로 확산된다.
     * - 따라서 category 루프 "안쪽"에서 try-catch를 수행하여, 실패 단위를 category로 고정한다.
     */
    private void crawlGroup(Map<String, String> noticeUrls) {
        noticeUrls.forEach((category, path) -> {
            try {
                crawlSingleCategory(category, path);
            } catch (Exception e) {
                log.error("[MJS] category={} 크롤링 실패. 다른 카테고리는 계속 진행합니다.", category, e);
            }
        });
    }

    /**
     * 특정 카테고리 크롤링.
     *
     * 성능/스레드 관점 핵심:
     * - 네트워크 I/O(목록/본문 HTTP 요청)는 시간이 오래 걸리며 스레드를 점유한다.
     * - 따라서 크롤링 단계에서는 트랜잭션을 열지 않는다.
     * - 크롤링 결과를 메모리 버퍼에 모아두었다가 마지막에 saveAll로 저장한다.
     *
     * 중단 정책:
     * - 오래된 공지를 만나면 더 이상 볼 필요가 없으므로 즉시 중단한다.
     * - 이미 DB에 존재하는 최신 공지를 만나면 "이 카테고리는 이미 최신 상태"로 보고 즉시 중단한다.
     */
    private void crawlSingleCategory(String category, String path) {

        int cutoffYear = LocalDate.now().getYear() - 2;
        int page = 1;
        boolean stop = false;

        /*
         * 저장 버퍼.
         * - DB 저장은 마지막에 한 번만 수행하여 트랜잭션 및 flush 횟수를 줄인다.
         * - 초기 크기는 임의값이며, 실제 공지 평균 수에 맞춰 조정할 수 있다.
         */
        List<Notice> toSave = new ArrayList<>(32);

        while (!stop) {

            /*
             * 목록 페이지 크롤링.
             * - Helper에서 네트워크/파싱/셀렉터를 담당한다.
             * - 여기서 예외가 발생하면 해당 카테고리만 중단된다(상위에서 category 단위 격리).
             */
            Elements rows = NoticeCrawlHelper.crawlList(path, page);
            if (rows.isEmpty()) {
                break;
            }

            for (Element row : rows) {
                /*
                 * row 처리 결과가 stop이면, 현재 카테고리 크롤링을 즉시 종료한다.
                 * - 중복 공지 발견 시, 이후 페이지에 더 최신 공지가 나올 가능성이 낮으므로 종료한다.
                 * - cutoffYear 기준으로 오래된 공지를 만나면 더 이상 의미가 없어 종료한다.
                 */
                stop = processRow(row, category, cutoffYear, toSave);
                if (stop) {
                    break;
                }
            }

            page++;
        }

        /*
         * DB 저장은 마지막에 한 번만 수행한다.
         * - 트랜잭션을 짧게 유지하여 커넥션 점유 시간을 최소화한다.
         * - saveAll이 "Spring Batch"는 아니지만, 반복 save()에 비해 flush/트랜잭션 비용이 줄어든다.
         * - 대량 insert 최적화가 필요하다면 hibernate.jdbc.batch_size 등의 설정을 추가로 고려한다.
         */
        applicationContext
                .getBean(NoticeCrawlingService.class)
                .saveNotices(toSave);

        log.info("[MJS] category={} 크롤링 종료. 저장 대상 {}건", category, toSave.size());
    }

    /**
     * 목록 row 단위 처리.
     *
     * 처리 순서(성능 최적화 관점):
     * 1) 목록 데이터 파싱/정규화 (가벼움)
     * 2) 중단 조건 검사(오래된 공지) (가벼움)
     * 3) 중복 존재 여부 검사(DB) (중간 비용)
     * 4) 상세 페이지 content 크롤링(네트워크 I/O) (가장 비쌈)
     *
     * 가장 비싼 본문 크롤링을 "중복 체크 이후"로 배치하여 불필요한 네트워크 요청을 최소화한다.
     *
     * @return true면 현재 카테고리 크롤링을 중단한다.
     */
    private boolean processRow(
            Element row,
            String category,
            int cutoffYear,
            List<Notice> toSave
    ) {

        // (1) 목록 페이지 데이터 추출
        String rawDate = row.select("._artclTdRdate").text();
        String rawTitle = row.select(".artclLinkView strong").text();
        String rawLink = row.select(".artclLinkView").attr("href");

        LocalDateTime date = normalizeDate(rawDate);
        String title = normalizeTitle(rawTitle);

        /*
         * 목록 파싱 결과가 비정상인 경우 해당 row는 건너뛴다.
         * - 이 경우는 실패가 아니라 "데이터 품질" 문제이므로 stop하지 않는다.
         */
        if (date == null || title.isEmpty()) {
            return false;
        }

        // (2) 오래된 공지면 중단
        if (date.getYear() <= cutoffYear) {
            return true;
        }

        // (3) 중복 공지 체크: 이미 DB에 있으면 해당 카테고리는 최신 상태로 보고 중단
        if (noticeRepository.existsByDateAndCategoryAndTitle(date, category, title)) {
            return true;
        }

        // (4) 상세 페이지 링크 생성
        String finalUrl = SUBVIEW_BASE + encodeArtclViewToEnc(rawLink);

        // (5) 상세 페이지 content 크롤링
        String content = NoticeCrawlHelper.crawlContent(finalUrl);

        // (6) 엔티티 생성 및 저장 버퍼 적재
        toSave.add(Notice.createNotice(title, content, date, category, finalUrl));

        return false;
    }

    /**
     * DB 저장 전용 트랜잭션.
     *
     * 주의:
     * - 이 메서드가 호출되는 시점에는 이미 네트워크 I/O 작업이 끝난 상태여야 한다.
     * - 트랜잭션 안에서 HTTP 요청을 수행하면, 커넥션과 트랜잭션이 불필요하게 오래 유지되어 성능이 악화된다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void saveNotices(List<Notice> notices) {
        if (!notices.isEmpty()) {
            noticeRepository.saveAll(notices);
        }
    }

    /* ===================== 문자열 정규화 ===================== */

    private LocalDateTime normalizeDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) return null;
        try {
            return LocalDate.parse(
                    rawDate.trim()
                            .replaceAll("\\s+", "")
                            .replaceAll("\\.\\s*", "-")
            ).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeTitle(String rawTitle) {
        return rawTitle == null ? "" : rawTitle.trim().replaceAll("\\s+", " ");
    }

    /* ===================== 상세 URL enc 생성 ===================== */

    /**
     * 공지 상세 페이지 enc 파라미터 생성.
     *
     * 설계 의도:
     * - 이 메서드는 순수 문자열 처리 로직만 수행한다.
     * - 네트워크 I/O, DB 접근, 트랜잭션과 무관하다.
     * - 상세 URL 생성 규칙이 바뀌면 이 메서드만 수정하면 된다.
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
}
