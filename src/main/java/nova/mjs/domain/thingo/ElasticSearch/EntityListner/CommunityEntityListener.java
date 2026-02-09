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
 * Community 엔티티 변경 감지 전용 리스너
 *
 * - 변경 감지 + 도메인 이벤트 발행만 담당
 * - Elasticsearch, Document, 전처리 로직에 관여하지 않는다
 */
@Component
@RequiredArgsConstructor
public class CommunityEntityListener {

    private final ApplicationEventPublisher eventPublisher;

    @PostPersist
    public void afterCreate(CommunityBoard board) {
        eventPublisher.publishEvent(
                CommunityIndexEvent.insert(board)
        );
    }

    @PostUpdate
    public void afterUpdate(CommunityBoard board) {
        eventPublisher.publishEvent(
                CommunityIndexEvent.update(board)
        );
    }

    @PostRemove
    public void afterDelete(CommunityBoard board) {
        eventPublisher.publishEvent(
                CommunityIndexEvent.delete(board.getUuid())
        );
    }
}
