package nova.mjs.util.ElasticSearch.EventSynchronization;

import lombok.RequiredArgsConstructor;
import nova.mjs.util.ElasticSearch.Document.SearchDocument;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchIndexPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public <T extends SearchDocument> void publish(T document, EntityIndexEvent.IndexAction action) {
        eventPublisher.publishEvent(new EntityIndexEvent<>(document, action));
    }
}
