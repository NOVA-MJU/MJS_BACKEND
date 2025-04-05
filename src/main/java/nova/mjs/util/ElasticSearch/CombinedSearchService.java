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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CombinedSearchService {
    private final NoticeRepository noticeRepository;
    private final NewsRepository newsRepository;
    private final CommunityBoardRepository communityBoardRepository;

    private final NoticeSearchRepository noticeSearchRepository;
    private final NewsSearchRepository newsSearchRepository;
    private final CommunitySearchRepository communitySearchRepository;

    public List<SearchDocument> unifiedSearch(String keyword) {
        List<SearchDocument> results = new ArrayList<>();

        results.addAll(noticeSearchRepository.searchByTitleOrContent(keyword));
        results.addAll(newsSearchRepository.searchByTitleOrContent(keyword));
        results.addAll(communitySearchRepository.searchByTitleOrContent(keyword));

        // Notice > News > Community 순 정렬
        results.sort(Comparator.comparing(doc -> switch (doc.getType()) {
            case "notice" -> 0;
            case "news" -> 1;
            case "community" -> 2;
            default -> 3;
        }));

        return results;
    }


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


}
