package nova.mjs.domain.thingo.ElasticSearch.indexing.Preprocessor.community;

import lombok.Getter;
import nova.mjs.domain.thingo.community.entity.CommunityBoard;

import java.util.UUID;

/**
 * Community 도메인 변경 이벤트
 *
 * - Elasticsearch, Document, 전처리 로직에 대한 의존 없음
 * - 도메인 변경 사실만 표현
 */
@Getter
public class CommunityIndexEvent {

    private final CommunityBoard community;
    private final UUID communityUuid;
    private final Action action;

    public enum Action {
        INSERT,
        UPDATE,
        DELETE
    }

    private CommunityIndexEvent(
            CommunityBoard community,
            UUID communityUuid,
            Action action
    ) {
        this.community = community;
        this.communityUuid = communityUuid;
        this.action = action;
    }

    public static CommunityIndexEvent insert(CommunityBoard community) {
        return new CommunityIndexEvent(community, null, Action.INSERT);
    }

    public static CommunityIndexEvent update(CommunityBoard community) {
        return new CommunityIndexEvent(community, null, Action.UPDATE);
    }

    /**
     * 삭제 이벤트
     *
     * - delete 시점에는 엔티티가 detach 될 수 있으므로
     *   UUID만 전달한다.
     */
    public static CommunityIndexEvent delete(UUID communityUuid) {
        return new CommunityIndexEvent(null, communityUuid, Action.DELETE);
    }

    public boolean isDelete() {
        return action == Action.DELETE;
    }
}
