package nova.mjs.util.ElasticSearch.EntityListner;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.util.ElasticSearch.Document.CommunityDocument;
import nova.mjs.util.ElasticSearch.EventSynchronization.EntityIndexEvent.IndexAction;
import nova.mjs.util.ElasticSearch.EventSynchronization.SearchIndexPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityEntityListener {
    private final SearchIndexPublisher publisher;

    @PostPersist
    public void afterCreate(CommunityBoard board) {
        publisher.publish(CommunityDocument.from(board), IndexAction.INSERT);
    }

    @PostUpdate
    public void afterUpdate(CommunityBoard board) {
        publisher.publish(CommunityDocument.from(board), IndexAction.UPDATE);
    }

    @PostRemove
    public void afterDelete(CommunityBoard board) {
        publisher.publish(CommunityDocument.builder()
                .id(board.getId().toString())
                .build(), IndexAction.DELETE);
    }
}