package nova.mjs.notice.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.notice.entity.Notice;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeSearchService {

    private final NoticeSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    //검색 메서드 - 오타 허용 + 형태소 분석 + 정확도 향상
    public List<NoticeResponseDto> searchByKeyword(String keyword) {
        long start = System.nanoTime();

        MatchQuery matchQuery = MatchQuery.of(m -> m
                .field("title")
                .query(keyword)
                .analyzer("nori")
                .fuzziness("AUTO")
                .operator(Operator.And)
        );

        Query query = NativeQuery.builder()
                .withQuery(matchQuery._toQuery())
                .build();

        SearchHits<NoticeSearchDocument> hits =
                elasticsearchOperations.search(query, NoticeSearchDocument.class);

        long end = System.nanoTime();
        log.info("[ElasticSearch] 한국어 검색 소요 시간: {}ms", (end - start) / 1_000_000.0);

        return hits.stream()
                .map(hit -> NoticeResponseDto.fromSearchDocument(hit.getContent()))
                .toList();
    }

    //Elasticsearch에 공지사항 저장
    public void saveNoticeToElasticsearch(Notice notice) {
        NoticeSearchDocument document = NoticeSearchDocument.fromEntity(notice);
        searchRepository.save(document);
        log.info("공지사항 저장 완료: {}", notice.getTitle());
    }
}
