package nova.mjs.util.ElasticSearch;

import lombok.RequiredArgsConstructor;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.news.entity.News;
import nova.mjs.news.repository.NewsRepository;
import nova.mjs.notice.entity.Notice;
import nova.mjs.notice.repository.NoticeRepository;
import nova.mjs.util.ElasticSearch.Document.CommunityDocument;
import nova.mjs.util.ElasticSearch.Document.NewsDocument;
import nova.mjs.util.ElasticSearch.Document.NoticeDocument;
import nova.mjs.util.ElasticSearch.Document.SearchDocument;
import nova.mjs.util.ElasticSearch.Repository.CommunitySearchRepository;
import nova.mjs.util.ElasticSearch.Repository.NewsSearchRepository;
import nova.mjs.util.ElasticSearch.Repository.NoticeSearchRepository;
import nova.mjs.util.ElasticSearch.Repository.SearchRepository;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CombinedSearchService {

    private final NoticeRepository noticeRepository;
    private final NewsRepository newsRepository;
    private final CommunityBoardRepository communityBoardRepository;

    private final NoticeSearchRepository noticeSearchRepository;
    private final NewsSearchRepository newsSearchRepository;
    private final CommunitySearchRepository communitySearchRepository;

    private final SearchRepository searchRepository;

    /**
     * 모든 데이터를 Elasticsearch에 동기화
     */
    public void syncAll() {
        // Notice
        List<Notice> notices = noticeRepository.findAll();
        List<NoticeDocument> noticeDocuments = notices.stream()
                .map(NoticeDocument::from)
                .toList();
        noticeSearchRepository.saveAll(noticeDocuments);

        // News
        List<News> newsList = newsRepository.findAll();
        List<NewsDocument> newsDocuments = newsList.stream()
                .map(NewsDocument::from)
                .toList();
        newsSearchRepository.saveAll(newsDocuments);

        // Community
        List<CommunityBoard> boards = communityBoardRepository.findAll();
        List<CommunityDocument> communityDocuments = boards.stream()
                .map(CommunityDocument::from)
                .toList();
        communitySearchRepository.saveAll(communityDocuments);
    }

    /**
     * @param keyword 검색어
     * @param type 필터 (notice / news / community)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 하이라이트 포함된 검색 결과 리스트
     */
    public List<SearchResponseDTO> unifiedSearch(String keyword, String type, int page, int size) {
        return searchRepository.search(keyword, type, page, size)
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
                searchHit.getContent().getType()
        );
    }
}