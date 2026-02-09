package nova.mjs.domain.thingo.ElasticSearch.indexing.Preprocessor.community;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.Document.CommunityDocument;
import nova.mjs.domain.thingo.ElasticSearch.indexing.event.EntityIndexEvent;
import nova.mjs.domain.thingo.ElasticSearch.indexing.publisher.SearchIndexPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CommunityIndexEvent → CommunityDocument 변환 후
 * Elasticsearch 색인 이벤트 발행
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
                            .id(event.getCommunityUuid().toString())
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
