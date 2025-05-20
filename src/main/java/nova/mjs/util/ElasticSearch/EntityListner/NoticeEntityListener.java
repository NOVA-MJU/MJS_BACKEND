package nova.mjs.util.ElasticSearch.EntityListner;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import nova.mjs.notice.entity.Notice;
import nova.mjs.util.ElasticSearch.Document.NoticeDocument;
import nova.mjs.util.ElasticSearch.EventSynchronization.EntityIndexEvent;
import nova.mjs.util.ElasticSearch.EventSynchronization.SearchIndexPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoticeEntityListener {
    private final SearchIndexPublisher publisher;

    @PostPersist
    public void afterCreate(Notice notice) {
        publisher.publish(NoticeDocument.from(notice), EntityIndexEvent.IndexAction.INSERT);
    }

    @PostUpdate
    public void afterSave(Notice notice) {
        publisher.publish(NoticeDocument.from(notice), EntityIndexEvent.IndexAction.UPDATE);
    }

    @PostRemove
    public void afterDelete(Notice notice) {
        publisher.publish(NoticeDocument.builder().id(notice.getId().toString()).build(), EntityIndexEvent.IndexAction.DELETE);
    }
}