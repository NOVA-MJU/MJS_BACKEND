package nova.mjs.domain.thingo.ElasticSearch.indexing.publisher;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.Document.SearchDocument;
import nova.mjs.domain.thingo.ElasticSearch.indexing.event.EntityIndexEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchIndexPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public <T extends SearchDocument> void publish(
            T document,
            EntityIndexEvent.IndexAction action
    ) {
        eventPublisher.publishEvent(
                new EntityIndexEvent<>(document, action)
        );
    }
}
