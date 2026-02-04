package nova.mjs.domain.mentorship.ElasticSearch.EntityListner;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.ElasticSearch.EventSynchronization.notice.NoticeIndexEvent;
import nova.mjs.domain.thingo.notice.entity.Notice;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoticeEntityListener {

    private final ApplicationEventPublisher eventPublisher;

    @PostPersist
    public void afterCreate(Notice notice) {
        eventPublisher.publishEvent(NoticeIndexEvent.insert(notice));
    }

    @PostUpdate
    public void afterUpdate(Notice notice) {
        eventPublisher.publishEvent(NoticeIndexEvent.update(notice));
    }

    @PostRemove
    public void afterDelete(Notice notice) {
        eventPublisher.publishEvent(NoticeIndexEvent.delete(notice.getId()));
    }
}
