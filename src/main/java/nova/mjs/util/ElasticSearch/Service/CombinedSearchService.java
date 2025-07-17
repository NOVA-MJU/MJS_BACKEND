package nova.mjs.util.ElasticSearch.Service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.broadcast.repository.BroadcastRepository;
import nova.mjs.domain.community.repository.CommunityBoardRepository;
import nova.mjs.domain.department.repository.DepartmentNoticeRepository;
import nova.mjs.domain.department.repository.DepartmentScheduleRepository;
import nova.mjs.domain.news.repository.NewsRepository;
import nova.mjs.domain.notice.repository.NoticeRepository;
import nova.mjs.util.ElasticSearch.Document.*;
import nova.mjs.util.ElasticSearch.Repository.*;
import nova.mjs.util.ElasticSearch.SearchResponseDTO;
import nova.mjs.util.ElasticSearch.SearchType;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CombinedSearchService {

    private final NoticeRepository noticeRepository;
    private final NewsRepository newsRepository;
    private final CommunityBoardRepository communityBoardRepository;
    private final DepartmentScheduleRepository departmentScheduleRepository;
    private final DepartmentNoticeRepository departmentNoticeRepository;
    private final BroadcastRepository broadcastRepository;

    private final NoticeSearchRepository noticeSearchRepository;
    private final NewsSearchRepository newsSearchRepository;
    private final CommunitySearchRepository communitySearchRepository;
    private final DepartmentScheduleSearchRepository departmentScheduleSearchRepository;
    private final DepartmentNoticeSearchRepository departmentNoticeSearchRepository;
    private final BroadcastSearchRepository broadcastSearchRepository;

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

    /**
     * @param keyword 검색어
     * @param type 필터 (
     *              notice              공지사항
     *              news                명대신문
     *              community           자유게시판
     *              departmentNotice    학과별 공지
     *              departmentSchedule  학과별 스케줄
     *              Broadcast           명대뉴스(명대방송국)
     *              미정                 학사일정
     *              )
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 하이라이트 포함된 검색 결과 리스트
     */
    public List<SearchResponseDTO> unifiedSearch(String keyword, String type, int page, int size) {
        SearchType searchType = SearchType.from(type);

        return searchRepository.search(keyword, searchType, page, size)
                .getSearchHits().stream()
                .map(searchHit -> convertToDTO((SearchHit<SearchDocument>) searchHit))
                .collect(Collectors.toList());
    }

    /**
     * SearchHit를 SearchResponseDTO로 변환
     * @param searchHit SearchHit 객체
     * @return 변환된 SearchResponseDTO
     */
    private SearchResponseDTO convertToDTO(SearchHit<SearchDocument> searchHit) {
        String highlightedTitle = searchHit.getHighlightFields().get("title") != null ?
                searchHit.getHighlightFields().get("title").get(0) : searchHit.getContent().getTitle();
        String highlightedContent = searchHit.getHighlightFields().get("content") != null ?
                searchHit.getHighlightFields().get("content").get(0) : searchHit.getContent().getContent();

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

    public Map<String, List<SearchResponseDTO>> searchTop5EachType(String keyword) {
        Map<String, List<SearchResponseDTO>> result = new LinkedHashMap<>();

        // 순서 : 공지사항 > 학사 일정(미정) > 학과 공지 > 학과 스케줄 > 자유게시판 > 명대신문 > 방송
        result.put("notice", unifiedSearch(keyword, "NOTICE", 0, 5));
        result.put("departmentSchedule", unifiedSearch(keyword, "DEPARTMENT_SCHEDULE", 0, 5));
        result.put("departmentNotice", unifiedSearch(keyword, "DEPARTMENT_NOTICE", 0, 5));
        result.put("community", unifiedSearch(keyword, "COMMUNITY", 0, 5));
        result.put("news", unifiedSearch(keyword, "NEWS", 0, 5));
        result.put("broadcast", unifiedSearch(keyword, "BROADCAST", 0, 5));

        return result;
    }
}