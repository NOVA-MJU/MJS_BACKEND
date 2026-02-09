package nova.mjs.domain.thingo.ElasticSearch.EntityListner;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.indexing.Preprocessor.community.CommunityIndexEvent;
import nova.mjs.domain.thingo.community.entity.CommunityBoard;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Community 엔티티 변경을 감지하여 도메인 이벤트를 발행한다.
 *
 * 원칙:
 * - JPA EntityListener는 "변경 감지 + 이벤트 발행"만 담당한다.
 * - Elasticsearch, Document, 전처리 로직은 절대 관여하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class CommunityEntityListener {

    private final ApplicationEventPublisher eventPublisher;

    @PostPersist
    public void afterCreate(CommunityBoard board) {
        eventPublisher.publishEvent(CommunityIndexEvent.insert(board));
    }

    @PostUpdate
    public void afterUpdate(CommunityBoard board) {
        eventPublisher.publishEvent(CommunityIndexEvent.update(board));
    }

    @PostRemove
    public void afterDelete(CommunityBoard board) {
        eventPublisher.publishEvent(CommunityIndexEvent.delete(board.getId()));
    }
}
