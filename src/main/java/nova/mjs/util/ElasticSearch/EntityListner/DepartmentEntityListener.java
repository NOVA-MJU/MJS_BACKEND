package nova.mjs.util.ElasticSearch.EntityListner;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.department.entity.DepartmentNotice;
import nova.mjs.domain.department.entity.DepartmentSchedule;
import nova.mjs.util.ElasticSearch.Document.DepartmentNoticeDocument;
import nova.mjs.util.ElasticSearch.Document.DepartmentScheduleDocument;
import nova.mjs.util.ElasticSearch.EventSynchronization.SearchIndexPublisher;
import nova.mjs.util.ElasticSearch.EventSynchronization.EntityIndexEvent.IndexAction;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepartmentEntityListener {
    private final SearchIndexPublisher publisher;

    // ====== DepartmentSchedule ======

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

    // ====== DepartmentNotice ======

    @PostPersist
    public void afterNoticeCreate(DepartmentNotice notice) {
        publisher.publish(DepartmentNoticeDocument.from(notice), IndexAction.INSERT);
    }

    @PostUpdate
    public void afterNoticeUpdate(DepartmentNotice notice) {
        publisher.publish(DepartmentNoticeDocument.from(notice), IndexAction.UPDATE);
    }

    @PostRemove
    public void afterNoticeDelete(DepartmentNotice notice) {
        publisher.publish(DepartmentNoticeDocument.builder()
                .id(notice.getUuid().toString())
                .build(), IndexAction.DELETE);
    }
}

