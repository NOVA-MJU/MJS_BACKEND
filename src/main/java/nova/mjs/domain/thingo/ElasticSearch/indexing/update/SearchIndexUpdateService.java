package nova.mjs.domain.thingo.ElasticSearch.indexing.update;

import java.util.UUID;

public interface SearchIndexUpdateService {

    /**
     * 커뮤니티 게시글 카운트 갱신
     *
     * - null 값은 해당 필드 업데이트를 생략한다.
     * - 검색/목록에서 DB count 호출을 제거하기 위해 ES 문서에 카운트를 저장한다.
     */
    void updateCommunityCounts(UUID boardUuid, Integer likeCount, Integer commentCount);

}
