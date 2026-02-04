package nova.mjs.domain.thingo.ElasticSearch.EventSynchronization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.ElasticSearch.Document.*;
import nova.mjs.domain.thingo.ElasticSearch.Repository.*;
import nova.mjs.domain.thingo.ElasticSearch.unified.UnifiedSearchMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexEventListener {

    private final NoticeSearchRepository noticeSearchRepository;
    private final NewsSearchRepository newsSearchRepository;
    private final CommunitySearchRepository communitySearchRepository;
    private final DepartmentScheduleSearchRepository departmentScheduleSearchRepository;
    private final DepartmentNoticeSearchRepository departmentNoticeSearchRepository;
    private final MjuCalendarSearchRepository mjuCalendarSearchRepository;

    private final UnifiedSearchRepository unifiedSearchRepository;
    private final UnifiedSearchMapper unifiedSearchMapper;

    /**
     * DB 트랜잭션이 "커밋된 이후"에만 ES 반영
     * - 롤백되었는데 ES에는 남는(유령 문서) 문제 방지
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEntityIndexEvent(EntityIndexEvent<? extends SearchDocument> event) {

        SearchDocument doc = event.getDocument();

        try {
            switch (event.getAction()) {
                case INSERT, UPDATE -> handleSave(doc);
                case DELETE -> handleDelete(doc);
            }

            log.info("[Elasticsearch] [{}] {} 성공 (ID: {})",
                    doc.getType(), event.getAction(), doc.getId());

        } catch (Exception e) {
            log.error("[Elasticsearch] [{}] {} 실패 (ID: {})",
                    doc.getType(), event.getAction(), doc.getId(), e);
        }
    }

    /**
     * INSERT/UPDATE 공통 처리
     * - 도메인 인덱스 저장
     * - 통합 인덱스 저장 (fan-out)
     */
    private void handleSave(SearchDocument doc) {

        executeByType(
                doc,
                noticeSearchRepository::save,
                newsSearchRepository::save,
                communitySearchRepository::save,
                departmentScheduleSearchRepository::save,
                departmentNoticeSearchRepository::save,
                mjuCalendarSearchRepository::save
        );

        // 통합 검색 인덱스 저장
        unifiedSearchRepository.save(unifiedSearchMapper.from(doc));
    }

    /**
     * DELETE 공통 처리
     * - 도메인 인덱스 삭제
     * - 통합 인덱스 삭제 (fan-out)
     *
     * 중요: 통합 인덱스의 _id 규칙은 일반적으로 TYPE:ORIGINAL_ID
     * 따라서 doc.getId()가 아닌 unifiedSearchMapper.buildId(doc) 사용
     */
    private void handleDelete(SearchDocument doc) {

        executeByType(
                doc,
                d -> noticeSearchRepository.deleteById(d.getId()),
                d -> newsSearchRepository.deleteById(d.getId()),
                d -> communitySearchRepository.deleteById(d.getId()),
                d -> departmentScheduleSearchRepository.deleteById(d.getId()),
                d -> departmentNoticeSearchRepository.deleteById(d.getId()),
                d -> mjuCalendarSearchRepository.deleteById(d.getId())
        );

        // 통합 검색 인덱스 삭제 (ID 규칙 일치가 핵심)
        unifiedSearchRepository.deleteById(unifiedSearchMapper.buildId(doc));
    }

    /**
     * "도메인 문서 타입"에 따라 주어진 동작을 실행한다.
     * - registry/handler 파일 추가 없이
     * - instanceof 분기 중복을 한 곳으로 모음
     */
    private void executeByType(
            SearchDocument doc,
            Consumer<NoticeDocument> noticeAction,
            Consumer<NewsDocument> newsAction,
            Consumer<CommunityDocument> communityAction,
            Consumer<DepartmentScheduleDocument> scheduleAction,
            Consumer<DepartmentNoticeDocument> deptNoticeAction,
            Consumer<MjuCalendarDocument> calendarAction
    ) {
        if (doc instanceof NoticeDocument notice) {
            noticeAction.accept(notice);

        } else if (doc instanceof NewsDocument news) {
            newsAction.accept(news);

        } else if (doc instanceof CommunityDocument comm) {
            communityAction.accept(comm);

        } else if (doc instanceof DepartmentScheduleDocument schedule) {
            scheduleAction.accept(schedule);

        } else if (doc instanceof DepartmentNoticeDocument deptNotice) {
            deptNoticeAction.accept(deptNotice);

        } else if (doc instanceof MjuCalendarDocument calendar) {
            calendarAction.accept(calendar);

        } else {
            // 앞으로 도메인/문서 타입이 늘어날 때,
            // 여기 로그가 뜨면 인덱싱 fan-out 누락을 즉시 감지 가능
            log.warn("[Elasticsearch] 처리되지 않은 문서 타입: {}", doc.getClass().getName());
        }
    }
}
