package nova.mjs.util.ElasticSearch.Service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.broadcast.repository.BroadcastRepository;
import nova.mjs.domain.calendar.repository.MjuCalendarRepository;
import nova.mjs.domain.community.repository.CommunityBoardRepository;
import nova.mjs.domain.department.repository.DepartmentNoticeRepository;
import nova.mjs.domain.department.repository.DepartmentScheduleRepository;
import nova.mjs.domain.news.repository.NewsRepository;
import nova.mjs.domain.notice.repository.NoticeRepository;
import nova.mjs.util.ElasticSearch.Document.*;
import nova.mjs.util.ElasticSearch.Repository.*;
import nova.mjs.util.ElasticSearch.SearchResponseDTO;
import nova.mjs.util.ElasticSearch.SearchType;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CombinedSearchService {

    private final NoticeRepository noticeRepository;
    private final NewsRepository newsRepository;
    private final CommunityBoardRepository communityBoardRepository;
    private final DepartmentScheduleRepository departmentScheduleRepository;
    private final DepartmentNoticeRepository departmentNoticeRepository;
    private final BroadcastRepository broadcastRepository;
    private final MjuCalendarRepository mjuCalendarRepository;


    private final NoticeSearchRepository noticeSearchRepository;
    private final NewsSearchRepository newsSearchRepository;
    private final CommunitySearchRepository communitySearchRepository;
    private final DepartmentScheduleSearchRepository departmentScheduleSearchRepository;
    private final DepartmentNoticeSearchRepository departmentNoticeSearchRepository;
    private final BroadcastSearchRepository broadcastSearchRepository;
    private final MjuCalendarSearchRepository mjuCalendarSearchRepository;


    private final SearchRepository searchRepository;

    /**
     * 모든 데이터를 Elasticsearch에 동기화
     */
    public void syncAll() {
        syncNotices();
        syncNews();
        syncCommunityBoards();
        syncDepartmentSchedules();
        syncDepartmentNotices();
        syncBroadcasts();
        syncMjuCalendars();
    }

    private void syncNotices() {
        List<NoticeDocument> docs = noticeRepository.findAll().stream()
                .map(NoticeDocument::from)
                .toList();
        noticeSearchRepository.saveAll(docs);
    }

    private void syncNews() {
        List<NewsDocument> docs = newsRepository.findAll().stream()
                .map(NewsDocument::from)
                .toList();
        newsSearchRepository.saveAll(docs);
    }

    private void syncCommunityBoards() {
        List<CommunityDocument> docs = communityBoardRepository.findAll().stream()
                .map(CommunityDocument::from)
                .toList();
        communitySearchRepository.saveAll(docs);
    }

    private void syncDepartmentSchedules() {
        List<DepartmentScheduleDocument> docs = departmentScheduleRepository.findAll().stream()
                .map(DepartmentScheduleDocument::from)
                .toList();
        departmentScheduleSearchRepository.saveAll(docs);
    }

    private void syncDepartmentNotices() {
        List<DepartmentNoticeDocument> docs = departmentNoticeRepository.findAll().stream()
                .map(DepartmentNoticeDocument::from)
                .toList();
        departmentNoticeSearchRepository.saveAll(docs);
    }

    private void syncBroadcasts() {
        List<BroadcastDocument> docs = broadcastRepository.findAll().stream()
                .map(BroadcastDocument::from)
                .toList();
        broadcastSearchRepository.saveAll(docs);
    }

    private void syncMjuCalendars() {
        List<MjuCalendarDocument> docs = mjuCalendarRepository.findAll().stream()
                .map(MjuCalendarDocument::from)
                .toList();
        mjuCalendarSearchRepository.saveAll(docs);
    }

    /**
     * @param keyword 검색어
     * @param type 필터 (
     *                 NOTICE               공지사항
     *                 DEPARTMENT_NOTICE    학과별 공지
     *                 DEPARTMENT_SCHEDULE  학과별 스케줄
     *                 COMMUNITY            자유게시판
     *                 NEWS                 명대신문
     *                 BROADCAST            명대뉴스(명대방송국)
     *                 MJU_CALENDER         학사일정
     *              )
     * @return 하이라이트 포함된 검색 결과 리스트
     */
    public Page<SearchResponseDTO> unifiedSearch(String keyword, String type, String order, Pageable pageable) {
        SearchType searchType = SearchType.from(type);

        SearchHits<? extends SearchDocument> hits =
                searchRepository.search(keyword, searchType, order, pageable);

        long total = hits.getTotalHits();

        List<SearchResponseDTO> content = hits.getSearchHits().stream()
                .map(sh -> convertToDTO((SearchHit<SearchDocument>) sh))
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * SearchHit를 SearchResponseDTO로 변환
     * @param searchHit SearchHit 객체
     * @return 변환된 SearchResponseDTO
     */
    private SearchResponseDTO convertToDTO(SearchHit<SearchDocument> searchHit) {
        Map<String, List<String>> hl = searchHit.getHighlightFields();

        String highlightedTitle =
                hl.containsKey("title") ? hl.get("title").get(0)
                        : hl.containsKey("title.keepdot") ? hl.get("title.keepdot").get(0)
                        : searchHit.getContent().getTitle();

        String highlightedContent =
                hl.containsKey("content") ? hl.get("content").get(0)
                        : hl.containsKey("content.keepdot") ? hl.get("content.keepdot").get(0)
                        : searchHit.getContent().getContent();

        return new SearchResponseDTO(
                searchHit.getContent().getId(),
                highlightedTitle,
                highlightedContent,
                searchHit.getContent().getDate(),
                searchHit.getContent().getLink(),
                searchHit.getContent().getCategory(),
                searchHit.getContent().getType(),
                searchHit.getContent().getImageUrl(),
                searchHit.getScore()
        );
    }

    public Map<String, List<SearchResponseDTO>> searchTop5EachType(String keyword, String order) {
        Map<String, List<SearchResponseDTO>> result = new LinkedHashMap<>();

        Pageable top5 = PageRequest.of(0, 5);

        // 순서 : 공지사항 > 학사 일정(미정) > 학과 공지 > 학과 스케줄 > 자유게시판 > 명대신문 > 방송
        result.put("notice",             unifiedSearch(keyword, "NOTICE",              order, top5).getContent());
        result.put("mjuCalendar",        unifiedSearch(keyword, "MJU_CALENDAR",        order, top5).getContent());
        result.put("departmentNotice",   unifiedSearch(keyword, "DEPARTMENT_NOTICE",   order, top5).getContent());
        result.put("departmentSchedule", unifiedSearch(keyword, "DEPARTMENT_SCHEDULE", order, top5).getContent());
        result.put("community",          unifiedSearch(keyword, "COMMUNITY",           order, top5).getContent());
        result.put("news",               unifiedSearch(keyword, "NEWS",                order, top5).getContent());
        result.put("broadcast",          unifiedSearch(keyword, "BROADCAST",           order, top5).getContent());

        return result;
    }
}