package nova.mjs.domain.thingo.ElasticSearch.indexing.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.ElasticSearch.Document.*;
import nova.mjs.domain.thingo.ElasticSearch.Repository.*;
import nova.mjs.domain.thingo.ElasticSearch.indexing.mapper.UnifiedSearchMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexEventListener {

    private final NoticeSearchRepository noticeSearchRepository;
    private final NewsSearchRepository newsSearchRepository;
    private final CommunitySearchRepository communitySearchRepository;
    private final DepartmentScheduleSearchRepository departmentScheduleSearchRepository;
    private final StudentCouncilNoticeSearchRepository studentCouncilNoticeSearchRepository;
    private final MjuCalendarSearchRepository mjuCalendarSearchRepository;
    private final BroadcastSearchRepository broadcastSearchRepository;

    private final UnifiedSearchRepository unifiedSearchRepository;
    private final UnifiedSearchMapper unifiedSearchMapper;

    /**
     * DB 트랜잭션이 "커밋된 이후"에만 ES 반영
     * - 롤백되었는데 ES에는 남는(유령 문서) 문제 방지
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEntityIndexEvent(EntityIndexEvent<? extends SearchDocument> event) {
        if (event == null || event.getDocument() == null || event.getAction() == null) {
            log.warn("[Elasticsearch] invalid event received. event={}", event);
            return;
        }

        SearchDocument doc = event.getDocument();

        try {
            switch (event.getAction()) {
                case INSERT, UPDATE -> handleSave(doc);
                case DELETE -> handleDelete(doc);
                default -> log.warn("[Elasticsearch] unsupported action. type={}, action={}, id={}",
                        safeType(doc), event.getAction(), safeId(doc));
            }

            log.info("[Elasticsearch] [{}] {} success (id={})",
                    safeType(doc), event.getAction(), safeId(doc));

        } catch (Exception e) {
            log.error("[Elasticsearch] [{}] {} failed (id={})",
                    safeType(doc), event.getAction(), safeId(doc), e);
        }
    }

    /**
     * INSERT/UPDATE 공통 처리
     * - 도메인 인덱스 저장
     * - 통합 인덱스 저장 (fan-out)
     */
    private void handleSave(SearchDocument doc) {
        saveToDomainIndex(doc);
        saveToUnifiedIndex(doc);
    }

    /**
     * DELETE 공통 처리
     * - 도메인 인덱스 삭제
     * - 통합 인덱스 삭제 (fan-out)
     *
     * 중요:
     * - 통합 인덱스의 _id 규칙은 TYPE:ORIGINAL_ID
     * - 따라서 unifiedSearchMapper.buildId(doc)로 삭제해야 한다.
     */
    private void handleDelete(SearchDocument doc) {
        deleteFromDomainIndex(doc);
        deleteFromUnifiedIndex(doc);
    }

    /**
     * 도메인 인덱스 저장 라우팅
     *
     * 원칙:
     * - "문서 변환"은 EntityListener(or Service)에서 끝내고,
     * - 여기서는 "문서 타입에 맞는 repository로 라우팅"만 한다.
     */
    private void saveToDomainIndex(SearchDocument doc) {
        if (doc instanceof NoticeDocument notice) {
            noticeSearchRepository.save(notice);
            return;
        }
        if (doc instanceof NewsDocument news) {
            newsSearchRepository.save(news);
            return;
        }
        if (doc instanceof CommunityDocument community) {
            communitySearchRepository.save(community);
            return;
        }
        if (doc instanceof DepartmentScheduleDocument schedule) {
            departmentScheduleSearchRepository.save(schedule);
            return;
        }
        if (doc instanceof StudentCouncilNoticeDocument deptNotice) {
            studentCouncilNoticeSearchRepository.save(deptNotice);
            return;
        }
        if (doc instanceof MjuCalendarDocument calendar) {
            mjuCalendarSearchRepository.save(calendar);
            return;
        }
        if (doc instanceof BroadcastDocument broadcast) {
            broadcastSearchRepository.save(broadcast);
            return;
        }

        log.warn("[Elasticsearch] save ignored - unsupported document type. class={}, type={}, id={}",
                doc.getClass().getName(), safeType(doc), safeId(doc));
    }

    /**
     * 도메인 인덱스 삭제 라우팅
     *
     * 주의:
     * - 도메인 인덱스는 보통 _id가 "original id" (예: uuid)로 저장된다.
     * - 통합 인덱스는 _id가 "TYPE:ORIGINAL_ID" 규칙이다.
     */
    private void deleteFromDomainIndex(SearchDocument doc) {
        String id = safeId(doc);

        if (doc instanceof NoticeDocument) {
            noticeSearchRepository.deleteById(id);
            return;
        }
        if (doc instanceof NewsDocument) {
            newsSearchRepository.deleteById(id);
            return;
        }
        if (doc instanceof CommunityDocument) {
            communitySearchRepository.deleteById(id);
            return;
        }
        if (doc instanceof DepartmentScheduleDocument) {
            departmentScheduleSearchRepository.deleteById(id);
            return;
        }
        if (doc instanceof StudentCouncilNoticeDocument) {
            studentCouncilNoticeSearchRepository.deleteById(id);
            return;
        }
        if (doc instanceof MjuCalendarDocument) {
            mjuCalendarSearchRepository.deleteById(id);
            return;
        }
        if (doc instanceof BroadcastDocument) {
            broadcastSearchRepository.deleteById(id);
            return;
        }

        log.warn("[Elasticsearch] delete ignored - unsupported document type. class={}, type={}, id={}",
                doc.getClass().getName(), safeType(doc), id);
    }

    /**
     * 통합 인덱스 저장
     */
    private void saveToUnifiedIndex(SearchDocument doc) {
        UnifiedSearchDocument unified = unifiedSearchMapper.from(doc);
        unifiedSearchRepository.save(unified);
    }

    /**
     * 통합 인덱스 삭제
     */
    private void deleteFromUnifiedIndex(SearchDocument doc) {
        String unifiedId = unifiedSearchMapper.buildId(doc);
        unifiedSearchRepository.deleteById(unifiedId);
    }

    private String safeId(SearchDocument doc) {
        return doc.getId() == null ? "" : doc.getId();
    }

    private String safeType(SearchDocument doc) {
        return doc.getType() == null ? "" : doc.getType();
    }
}
