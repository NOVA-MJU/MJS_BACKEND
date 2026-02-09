package nova.mjs.domain.thingo.ElasticSearch.indexing.Preprocessor.community;

import lombok.Getter;
import nova.mjs.domain.thingo.community.entity.CommunityBoard;

/**
 * Community 도메인 변경에 따른
 * Elasticsearch 색인을 위한 도메인 이벤트.
 *
 * 역할:
 * - Community 엔티티의 생성 / 수정 / 삭제 사실만을 표현한다.
 * - Elasticsearch, Document, 전처리 로직에 대한 의존은 절대 갖지 않는다.
 *
 * 처리 흐름:
 * CommunityEntityListener
 *  → CommunityIndexEvent
 *  → CommunityIndexEventHandler
 *  → EntityIndexEvent
 */
@Getter
public class CommunityIndexEvent {

    private final CommunityBoard community;
    private final Long communityId;
    private final Action action;

    public enum Action {
        INSERT,
        UPDATE,
        DELETE
    }

    private CommunityIndexEvent(
            CommunityBoard community,
            Long communityId,
            Action action
    ) {
        this.community = community;
        this.communityId = communityId;
        this.action = action;
    }

    /**
     * Community 생성 이벤트
     */
    public static CommunityIndexEvent insert(CommunityBoard community) {
        return new CommunityIndexEvent(community, null, Action.INSERT);
    }

    /**
     * Community 수정 이벤트
     */
    public static CommunityIndexEvent update(CommunityBoard community) {
        return new CommunityIndexEvent(community, null, Action.UPDATE);
    }

    /**
     * Community 삭제 이벤트
     *
     * 주의:
     * - 삭제 시에는 엔티티가 detach 상태일 수 있으므로
     *   ID만 전달한다.
     */
    public static CommunityIndexEvent delete(Long communityId) {
        return new CommunityIndexEvent(null, communityId, Action.DELETE);
    }

    /**
     * 삭제 이벤트 여부
     */
    public boolean isDelete() {
        return action == Action.DELETE;
    }
}
