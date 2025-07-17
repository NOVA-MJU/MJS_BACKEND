package nova.mjs.util.ElasticSearch.Repository;

import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import lombok.RequiredArgsConstructor;
import nova.mjs.util.ElasticSearch.Document.*;
import nova.mjs.util.ElasticSearch.SearchType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchRepository {

    private final ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public SearchHits<? extends SearchDocument> search(String keyword, SearchType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<HighlightField> highlightFields = List.of(
                new HighlightField("title"),
                new HighlightField("content")
        );

        // 지정된 type에 맞는 SearchDocument 클래스를 결정
        Class<? extends SearchDocument> targetClass = resolveTargetClass(type);

        HighlightQuery highlightQuery = new HighlightQuery(
                new Highlight(highlightFields), targetClass
        );

        /**
         * Spring Data Elasticsearch DSL
         */

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .functionScore(fs -> fs
                                .query(inner -> inner
                                        .bool(b -> b
                                                .should(s -> s.match(m -> m.field("title").query(keyword)))
                                                .should(s -> s.match(m -> m.field("content").query(keyword)))
                                        )
                                )
                                .functions(List.of(
                                        FunctionScore.of(f -> f
                                                .filter(q1 -> q1.matchPhrase(mp -> mp.field("title").query(keyword)))
                                                .weight(10.0)
                                        ),
                                        FunctionScore.of(f -> f
                                                .filter(q2 -> q2.matchPhrase(mp -> mp.field("content").query(keyword)))
                                                .weight(8.0)
                                        ),
                                        FunctionScore.of(f -> f
                                                .filter(q3 -> q3.match(m -> m.field("title").query(keyword)))
                                                .weight(2.0)
                                        ),
                                        FunctionScore.of(f -> f
                                                .filter(q4 -> q4.match(m -> m.field("content").query(keyword)))
                                                .weight(1.0)
                                        )
                                ))
                                .scoreMode(FunctionScoreMode.Sum) // 가중치들을 모두 더함 (title+content 다 걸리면 스코어 높아짐)
                                .boostMode(FunctionBoostMode.Sum) // 기존 스코어와 가중치를 더해서 최종 점수 계산
                        )
                )
                .withHighlightQuery(highlightQuery)
                .withPageable(pageable)
                .build();

    // 검색 후, 결과를 반환
        return elasticsearchTemplate.search(query, targetClass);
    }

    private Class<? extends SearchDocument> resolveTargetClass(SearchType type) {
        return switch (type) {
            case NOTICE -> NoticeDocument.class;
            case MJU_CALENDAR -> MjuCalendarDocument.class;
            case DEPARTMENT_NOTICE -> DepartmentNoticeDocument.class;
            case DEPARTMENT_SCHEDULE -> DepartmentScheduleDocument.class;
            case COMMUNITY -> CommunityDocument.class;
            case NEWS -> NewsDocument.class;
            case BROADCAST -> BroadcastDocument.class;
        };
    }
}