package nova.mjs.util.ElasticSearch.EventSynchronization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.ElasticSearch.Document.*;
import nova.mjs.util.ElasticSearch.Repository.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexEventListener {
    private final NoticeSearchRepository noticeSearchRepository;
    private final NewsSearchRepository newsSearchRepository;
    private final CommunitySearchRepository communitySearchRepository;
    private final DepartmentScheduleSearchRepository departmentScheduleSearchRepository;
    private final DepartmentNoticeSearchRepository departmentNoticeSearchRepository;
    private final MjuCalendarSearchRepository mjuCalendarRepository;



    @EventListener
    public void handleEntityIndexEvent(EntityIndexEvent<? extends SearchDocument> event) {
        SearchDocument doc = event.getDocument();
        try {
            switch (event.getAction()) {
                case INSERT, UPDATE -> {
                    if (doc instanceof NoticeDocument notice) {
                        noticeSearchRepository.save(notice);
                    } else if (doc instanceof NewsDocument news) {
                        newsSearchRepository.save(news);
                    } else if (doc instanceof CommunityDocument comm) {
                        communitySearchRepository.save(comm);
                    } else if (doc instanceof DepartmentScheduleDocument schedule) {
                        departmentScheduleSearchRepository.save(schedule);
                    } else if (doc instanceof DepartmentNoticeDocument deptNotice) {
                        departmentNoticeSearchRepository.save(deptNotice);
                    } else if (doc instanceof MjuCalendarDocument mjuCalendar) {
                        mjuCalendarRepository.save(mjuCalendar);
                    }
                    log.info("[Elasticsearch] [{}] 문서 {} 처리 성공 (ID: {})",
                            doc.getType(), event.getAction(), doc.getId());
                }

                case DELETE -> {
                    if (doc instanceof NoticeDocument notice) {
                        noticeSearchRepository.deleteById(notice.getId());
                    } else if (doc instanceof NewsDocument news) {
                        newsSearchRepository.deleteById(news.getId());
                    } else if (doc instanceof CommunityDocument comm) {
                        communitySearchRepository.deleteById(comm.getId());
                    } else if (doc instanceof DepartmentScheduleDocument schedule) {
                        departmentScheduleSearchRepository.deleteById(schedule.getId());
                    } else if (doc instanceof DepartmentNoticeDocument deptNotice) {
                        departmentNoticeSearchRepository.deleteById(deptNotice.getId());
                    } else if (doc instanceof MjuCalendarDocument mjuCalendar) {
                        mjuCalendarRepository.deleteById(mjuCalendar.getId());
                    }

                    log.info("[Elasticsearch] [{}] 문서 삭제 성공 (ID: {})", doc.getType(), doc.getId());
                }
            }

        } catch (Exception e) {
            log.error("[Elasticsearch] [{}] 문서 {} 처리 실패 (ID: {}) - {}",
                    doc.getType(), event.getAction(), doc.getId(), e.getMessage(), e);
        }
    }
}
