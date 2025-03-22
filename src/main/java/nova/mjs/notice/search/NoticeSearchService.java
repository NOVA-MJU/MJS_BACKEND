package nova.mjs.notice.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.notice.dto.NoticeResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeSearchService {

    private final NoticeSearchRepository searchRepository;

    public List<NoticeResponseDto> searchByKeyword(String keyword) {
        long start = System.nanoTime(); // 성능 측정 시작

        List<NoticeSearchDocument> result = searchRepository.findByTitleContaining(keyword);

        long end = System.nanoTime(); // 성능 측정 종료
        double elapsedMs = (end - start) / 1_000_000.0;
        log.info("[ElasticSearch] 검색 소요 시간: {}ms", elapsedMs);

        return result.stream()
                .map(NoticeResponseDto::fromSearchDocument)
                .toList();
    }
}
