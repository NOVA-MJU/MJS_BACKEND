package nova.mjs.util.ElasticSearch.EntityListner;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.department.entity.DepartmentNotice;
import nova.mjs.util.ElasticSearch.Document.DepartmentNoticeDocument;
import nova.mjs.util.ElasticSearch.EventSynchronization.EntityIndexEvent;
import nova.mjs.util.ElasticSearch.EventSynchronization.SearchIndexPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepartmentNoticeEntityListener {
    private final SearchIndexPublisher publisher;

    @PostPersist
    public void afterNoticeCreate(DepartmentNotice notice) {
        publisher.publish(DepartmentNoticeDocument.from(notice), EntityIndexEvent.IndexAction.INSERT);
    }

    @PostUpdate
    public void afterNoticeUpdate(DepartmentNotice notice) {
        publisher.publish(DepartmentNoticeDocument.from(notice), EntityIndexEvent.IndexAction.UPDATE);
    }

    @PostRemove
    public void afterNoticeDelete(DepartmentNotice notice) {
        publisher.publish(DepartmentNoticeDocument.builder()
                .id(notice.getUuid().toString())
                .build(), EntityIndexEvent.IndexAction.DELETE);
    }
}
