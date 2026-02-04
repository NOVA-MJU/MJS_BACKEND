package nova.mjs.domain.thingo.ElasticSearch.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.ElasticSearch.Document.*;
import nova.mjs.domain.thingo.ElasticSearch.EventSynchronization.SearchContentPreprocessor;
import nova.mjs.domain.thingo.ElasticSearch.Repository.*;
import nova.mjs.domain.thingo.ElasticSearch.unified.UnifiedSearchMapper;
import nova.mjs.domain.thingo.broadcast.repository.BroadcastRepository;
import nova.mjs.domain.thingo.calendar.repository.MjuCalendarRepository;
import nova.mjs.domain.thingo.community.repository.CommunityBoardRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentNoticeRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentScheduleRepository;
import nova.mjs.domain.thingo.news.repository.NewsRepository;
import nova.mjs.domain.thingo.notice.repository.NoticeRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * SearchIndexSyncService
 *
 * 역할
 * - DB → 도메인 Elasticsearch 인덱스 동기화
 * - 모든 도메인 인덱스 동기화 완료 후 Unified 인덱스 재생성
 *
 * 설계 원칙
 * - Unified 인덱스는 파생 인덱스
 * - 도메인 ES 기준으로만 생성
 * - 항상 drop & recreate 전략
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchIndexSyncService {

    /* =========================
       RDB Repositories
       ========================= */

    private final NoticeRepository noticeRepository;
    private final NewsRepository newsRepository;
    private final CommunityBoardRepository communityBoardRepository;
    private final DepartmentScheduleRepository departmentScheduleRepository;
    private final DepartmentNoticeRepository departmentNoticeRepository;
    private final BroadcastRepository broadcastRepository;
    private final MjuCalendarRepository mjuCalendarRepository;

    /* =========================
       Elasticsearch Repositories
       ========================= */

    private final NoticeSearchRepository noticeSearchRepository;
    private final NewsSearchRepository newsSearchRepository;
    private final CommunitySearchRepository communitySearchRepository;
    private final DepartmentScheduleSearchRepository departmentScheduleSearchRepository;
    private final DepartmentNoticeSearchRepository departmentNoticeSearchRepository;
    private final BroadcastSearchRepository broadcastSearchRepository;
    private final MjuCalendarSearchRepository mjuCalendarSearchRepository;

    private final UnifiedSearchRepository unifiedSearchRepository;
    private final UnifiedSearchMapper unifiedSearchMapper;

    private final ElasticsearchOperations elasticsearchOperations;
    private final SearchContentPreprocessor searchContentPreprocessor;

    /**
     * Controller 단일 진입점
     */
    public void syncAll() {
        log.info("[SEARCH][SYNC][ALL] start");

        syncDomainIndexes();
        rebuildUnifiedIndex();

        log.info("[SEARCH][SYNC][ALL] end");
    }

    /* ==================================================
       1. DB → 도메인 Elasticsearch 동기화
       ================================================== */

    private void syncDomainIndexes() {

        // Notice만 전처리 적용
        syncWithPreprocessor(
                "NOTICE",
                noticeRepository.findAll(),
                NoticeDocument::from,
                noticeSearchRepository
        );

        // 나머지는 순수 변환
        sync(
                "NEWS",
                newsRepository.findAll(),
                NewsDocument::from,
                newsSearchRepository
        );

        sync(
                "COMMUNITY",
                communityBoardRepository.findAll(),
                CommunityDocument::from,
                communitySearchRepository
        );

        sync(
                "DEPARTMENT_SCHEDULE",
                departmentScheduleRepository.findAll(),
                DepartmentScheduleDocument::from,
                departmentScheduleSearchRepository
        );

        sync(
                "DEPARTMENT_NOTICE",
                departmentNoticeRepository.findAll(),
                DepartmentNoticeDocument::from,
                departmentNoticeSearchRepository
        );

        sync(
                "BROADCAST",
                broadcastRepository.findAll(),
                BroadcastDocument::from,
                broadcastSearchRepository
        );

        sync(
                "MJU_CALENDAR",
                mjuCalendarRepository.findAll(),
                MjuCalendarDocument::from,
                mjuCalendarSearchRepository
        );
    }

    /**
     * 전처리가 필요 없는 일반 도메인 sync
     */
    private <E, D> void sync(
            String domainName,
            List<E> entities,
            Function<E, D> mapper,
            ElasticsearchRepository<D, ?> repository
    ) {
        List<D> documents = entities.stream()
                .map(mapper)
                .toList();

        repository.saveAll(documents);

        log.info("[SEARCH][SYNC][{}] count={}", domainName, documents.size());
    }

    /**
     * 전처리가 필요한 도메인 전용 sync (Notice)
     */
    private <E, D> void syncWithPreprocessor(
            String domainName,
            List<E> entities,
            BiFunction<E, SearchContentPreprocessor, D> mapper,
            ElasticsearchRepository<D, ?> repository
    ) {
        List<D> documents = entities.stream()
                .map(entity -> mapper.apply(entity, searchContentPreprocessor))
                .toList();

        repository.saveAll(documents);

        log.info("[SEARCH][SYNC][{}] count={}", domainName, documents.size());
    }

    /* ==================================================
       2. 도메인 Elasticsearch → Unified 재생성
       ================================================== */

    private void rebuildUnifiedIndex() {

        log.info("[SEARCH][UNIFIED][REBUILD] start");

        IndexOperations indexOps =
                elasticsearchOperations.indexOps(UnifiedSearchDocument.class);

        if (indexOps.exists()) {
            indexOps.delete();
            log.info("[SEARCH][UNIFIED] index deleted");
        }

        indexOps.create();
        indexOps.putMapping(indexOps.createMapping());
        log.info("[SEARCH][UNIFIED] index created");

        rebuildFrom(noticeSearchRepository.findAll());
        rebuildFrom(newsSearchRepository.findAll());
        rebuildFrom(communitySearchRepository.findAll());
        rebuildFrom(departmentScheduleSearchRepository.findAll());
        rebuildFrom(departmentNoticeSearchRepository.findAll());
        rebuildFrom(broadcastSearchRepository.findAll());
        rebuildFrom(mjuCalendarSearchRepository.findAll());

        log.info("[SEARCH][UNIFIED][REBUILD] end");
    }

    /**
     * 도메인 SearchDocument → UnifiedSearchDocument 변환
     */
    private <T extends SearchDocument> void rebuildFrom(Iterable<T> domainDocuments) {

        List<UnifiedSearchDocument> unifiedDocuments =
                StreamSupport.stream(domainDocuments.spliterator(), false)
                        .map(unifiedSearchMapper::from)
                        .toList();

        unifiedSearchRepository.saveAll(unifiedDocuments);
    }
}
