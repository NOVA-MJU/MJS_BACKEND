package nova.mjs.util.ElasticSearch.EntityListner;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import nova.mjs.news.entity.News;
import nova.mjs.util.ElasticSearch.Document.NewsDocument;
import nova.mjs.util.ElasticSearch.EventSynchronization.EntityIndexEvent.IndexAction;
import nova.mjs.util.ElasticSearch.EventSynchronization.SearchIndexPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewsEntityListener {
    private final SearchIndexPublisher publisher;

    @PostPersist
    public void afterCreate(News news) {
        publisher.publish(NewsDocument.from(news), IndexAction.INSERT);
    }

    @PostUpdate
    public void afterUpdate(News news) {
        publisher.publish(NewsDocument.from(news), IndexAction.UPDATE);
    }

    @PostRemove
    public void afterDelete(News news) {
        publisher.publish(NewsDocument.builder()
                .id(news.getId().toString())
                .build(), IndexAction.DELETE);
    }
}