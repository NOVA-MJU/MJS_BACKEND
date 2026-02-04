package nova.mjs.domain.mentorship.ElasticSearch.Repository;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.ElasticSearch.Document.UnifiedSearchDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;
import co.elastic.clients.elasticsearch._types.SortOrder;


/**
 * UnifiedSearchQueryRepository
 *
 * 검색 전용 Repository.
 * - Elasticsearch 저장(CRUD)은 담당하지 않는다.
 * - 검색 조건, 가중치(weight), 정렬(order), 필터(type)를 모두 이 계층에서 제어한다.
 *
 * Service 계층은 "무엇을 검색할지"만 결정하고,
 * "어떻게 검색할지"는 이 Repository가 책임진다.
 */


@Repository
@RequiredArgsConstructor
public class UnifiedSearchQueryRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 통합 검색 쿼리 실행
     *
     * @param keyword   검색 키워드
     * @param type      문서 타입 필터 (null 허용)
     * @param order     정렬 기준 (relevance | latest | oldest)
     * @param pageable  페이징 정보
     */
    public SearchHits<UnifiedSearchDocument> search(
            String keyword,
            String type,
            String order,
            Pageable pageable
    ) {

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query -> query.bool(boolQuery -> {

                    /*
                     * 1. 키워드 기반 가중치 부여 검색
                     *
                     * title        : 가장 중요한 필드 (가중치 높음)
                     * title.ngram  : 부분 검색 대응
                     * category     : 문서 성격을 드러내는 보조 신호
                     * content      : 내용 전문 검색 (가중치 낮음)
                     *
                     * UX 관점에서 "제목 중심 검색"을 강제하기 위한 설계
                     */
                    boolQuery.must(mustQuery ->
                            mustQuery.multiMatch(multiMatchQuery ->
                                    multiMatchQuery
                                            .query(keyword)
                                            .fields(
                                                    "title^4",
                                                    "title.ngram^3",
                                                    "category^2",
                                                    "content^1"
                                            )
                            )
                    );

                    /*
                     * 2. 타입 필터
                     *
                     * overview / detail 검색에서 공통 사용
                     * type이 null이면 전체 검색
                     */
                    if (type != null) {
                        boolQuery.filter(filterQuery ->
                                filterQuery.term(termQuery ->
                                        termQuery
                                                .field("type")
                                                .value(type)
                                )
                        );
                    }

                    return boolQuery;
                }))
                /*
                 * 3. 정렬 전략
                 *
                 * relevance : Elasticsearch score 기준
                 * latest    : 최신 문서 우선
                 * oldest    : 오래된 문서 우선
                 */
                .withSort(sortBuilder -> {
                    if ("latest".equals(order)) {
                        return sortBuilder.field(fieldSort ->
                                fieldSort.field("date").order(SortOrder.Desc)
                        );
                    }

                    if ("oldest".equals(order)) {
                        return sortBuilder.field(fieldSort ->
                                fieldSort.field("date").order(SortOrder.Asc)
                        );
                    }

                    return sortBuilder.score(scoreSort -> scoreSort);
                })
                .withPageable(pageable)
                .build();

        return elasticsearchOperations.search(nativeQuery, UnifiedSearchDocument.class);
    }
}
