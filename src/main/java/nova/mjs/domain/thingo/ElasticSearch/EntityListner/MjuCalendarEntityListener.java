package nova.mjs.domain.thingo.ElasticSearch.EntityListner;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.calendar.entity.MjuCalendar;
import nova.mjs.domain.thingo.ElasticSearch.Document.MjuCalendarDocument;
import nova.mjs.domain.thingo.ElasticSearch.indexing.event.EntityIndexEvent;
import nova.mjs.domain.thingo.ElasticSearch.indexing.publisher.SearchIndexPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MjuCalendarEntityListener {
    private final SearchIndexPublisher publisher;

    @PostPersist
    public void afterCreate(MjuCalendar mjuCalendar) {
        publisher.publish(MjuCalendarDocument.from(mjuCalendar), EntityIndexEvent.IndexAction.INSERT);
    }

    @PostUpdate
    public void afterUpdate(MjuCalendar mjuCalendar) {
        publisher.publish(MjuCalendarDocument.from(mjuCalendar), EntityIndexEvent.IndexAction.UPDATE);
    }

    @PostRemove
    public void afterDelete(MjuCalendar mjuCalendar) {
        publisher.publish(MjuCalendarDocument.builder()
                .id(mjuCalendar.getId().toString())
                .build(), EntityIndexEvent.IndexAction.DELETE);
    }
}
