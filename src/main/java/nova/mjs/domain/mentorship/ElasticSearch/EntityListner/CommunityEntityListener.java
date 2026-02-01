package nova.mjs.domain.mentorship.ElasticSearch.EntityListner;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.community.entity.CommunityBoard;
import nova.mjs.domain.mentorship.ElasticSearch.Document.CommunityDocument;
import nova.mjs.domain.mentorship.ElasticSearch.EventSynchronization.EntityIndexEvent.IndexAction;
import nova.mjs.domain.mentorship.ElasticSearch.EventSynchronization.SearchIndexPublisher;
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
                .id(String.valueOf(board.getUuid()))
                .build(), IndexAction.DELETE);
    }
}