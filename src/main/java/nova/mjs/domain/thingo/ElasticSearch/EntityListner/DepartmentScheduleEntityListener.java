package nova.mjs.domain.thingo.ElasticSearch.EntityListner;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.entity.DepartmentSchedule;
import nova.mjs.domain.thingo.ElasticSearch.Document.DepartmentScheduleDocument;
import nova.mjs.domain.thingo.ElasticSearch.EventSynchronization.SearchIndexPublisher;
import nova.mjs.domain.thingo.ElasticSearch.EventSynchronization.EntityIndexEvent.IndexAction;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepartmentScheduleEntityListener {
    private final SearchIndexPublisher publisher;

    @PostPersist
    public void afterScheduleCreate(DepartmentSchedule schedule) {
        publisher.publish(DepartmentScheduleDocument.from(schedule), IndexAction.INSERT);
    }

    @PostUpdate
    public void afterScheduleUpdate(DepartmentSchedule schedule) {
        publisher.publish(DepartmentScheduleDocument.from(schedule), IndexAction.UPDATE);
    }

    @PostRemove
    public void afterScheduleDelete(DepartmentSchedule schedule) {
        publisher.publish(DepartmentScheduleDocument.builder()
                .id(schedule.getDepartmentScheduleUuid().toString())
                .build(), IndexAction.DELETE);
    }
}

