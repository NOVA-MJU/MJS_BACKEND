package nova.mjs.domain.thingo.ElasticSearch.EntityListner;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.Document.StudentCouncilNoticeDocument;
import nova.mjs.domain.thingo.department.entity.StudentCouncilNotice;
import nova.mjs.domain.thingo.ElasticSearch.indexing.event.EntityIndexEvent;
import nova.mjs.domain.thingo.ElasticSearch.indexing.publisher.SearchIndexPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentCouncilNoticeEntityListener {
    private final SearchIndexPublisher publisher;

    @PostPersist
    public void afterNoticeCreate(StudentCouncilNotice notice) {
        publisher.publish(StudentCouncilNoticeDocument.from(notice), EntityIndexEvent.IndexAction.INSERT);
    }

    @PostUpdate
    public void afterNoticeUpdate(StudentCouncilNotice notice) {
        publisher.publish(StudentCouncilNoticeDocument.from(notice), EntityIndexEvent.IndexAction.UPDATE);
    }

    @PostRemove
    public void afterNoticeDelete(StudentCouncilNotice notice) {
        publisher.publish(StudentCouncilNoticeDocument.builder()
                .id(notice.getUuid().toString())
                .build(), EntityIndexEvent.IndexAction.DELETE);
    }
}
