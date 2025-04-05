package nova.mjs.util.ElasticSearch.EventSynchronization;

import lombok.RequiredArgsConstructor;
import nova.mjs.util.ElasticSearch.Document.CommunityDocument;
import nova.mjs.util.ElasticSearch.Document.NewsDocument;
import nova.mjs.util.ElasticSearch.Document.NoticeDocument;
import nova.mjs.util.ElasticSearch.Document.SearchDocument;
import nova.mjs.util.ElasticSearch.Repository.CommunitySearchRepository;
import nova.mjs.util.ElasticSearch.Repository.NewsSearchRepository;
import nova.mjs.util.ElasticSearch.Repository.NoticeSearchRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchIndexEventListener {
    private final NoticeSearchRepository noticeSearchRepository;
    private final NewsSearchRepository newsSearchRepository;
    private final CommunitySearchRepository communitySearchRepository;

    @EventListener
    public void handleEntityIndexEvent(EntityIndexEvent<? extends SearchDocument> event) {
        SearchDocument doc = event.getDocument();
        switch (event.getAction()) {
            case INSERT, UPDATE -> {
                if (doc instanceof NoticeDocument notice) {
                    noticeSearchRepository.save(notice);
                } else if (doc instanceof NewsDocument news) {
                    newsSearchRepository.save(news);
                } else if (doc instanceof CommunityDocument comm) {
                    communitySearchRepository.save(comm);
                }
            }
            case DELETE -> {
                if (doc instanceof NoticeDocument notice) {
                    noticeSearchRepository.deleteById(notice.getId());
                } else if (doc instanceof NewsDocument news) {
                    newsSearchRepository.deleteById(news.getId());
                } else if (doc instanceof CommunityDocument comm) {
                    communitySearchRepository.deleteById(comm.getId());
                }
            }
        }
    }
}
