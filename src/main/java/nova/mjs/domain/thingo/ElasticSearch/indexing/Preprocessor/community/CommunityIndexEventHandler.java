package nova.mjs.domain.thingo.ElasticSearch.indexing.Preprocessor.community;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.Document.CommunityDocument;
import nova.mjs.domain.thingo.ElasticSearch.indexing.event.EntityIndexEvent;
import nova.mjs.domain.thingo.ElasticSearch.indexing.publisher.SearchIndexPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Community 도메인 변경 이벤트를 수신하여
 * Elasticsearch 색인용 CommunityDocument로 변환 후
 * 색인 이벤트를 발행한다.
 *
 * - 전처리는 이 단계에서 완료된다.
 * - Elasticsearch 계층에서는 Community 도메인 지식을 알지 않는다.
 */
@Component
@RequiredArgsConstructor
public class CommunityIndexEventHandler {

    private final CommunityContentPreprocessor preprocessor;
    private final SearchIndexPublisher searchIndexPublisher;

    @EventListener
    public void handle(CommunityIndexEvent event) {

        if (event.isDelete()) {
            searchIndexPublisher.publish(
                    CommunityDocument.builder()
                            .id(event.getCommunityId().toString())
                            .build(),
                    EntityIndexEvent.IndexAction.DELETE
            );
            return;
        }

        CommunityDocument document =
                CommunityDocument.from(event.getCommunity(), preprocessor);

        searchIndexPublisher.publish(
                document,
                event.getAction() == CommunityIndexEvent.Action.INSERT
                        ? EntityIndexEvent.IndexAction.INSERT
                        : EntityIndexEvent.IndexAction.UPDATE
        );
    }
}
