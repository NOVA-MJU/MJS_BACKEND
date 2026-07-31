package nova.mjs.domain.thingo.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.search.entity.UnifiedSearchIndex;
import nova.mjs.domain.thingo.search.repository.UnifiedSearchIndexRepository;
import nova.mjs.domain.thingo.semantic.NoticeSemanticClassifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Topic Catalog/분류 규칙 변경 뒤 기존 검색 인덱스를 일괄 재분류하는 내부 서비스. */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeSemanticReclassificationService {

    private static final int BATCH_SIZE = 200;

    private final UnifiedSearchIndexRepository repository;
    private final NoticeSemanticClassifier classifier;

    @Transactional
    public ReclassificationResult reclassifyAll() {
        long total = repository.count();
        int processed = 0;
        int classified = 0;
        List<String> failedIds = new ArrayList<>();

        for (int pageNumber = 0; processed < total; pageNumber++) {
            var page = repository.findAll(PageRequest.of(pageNumber, BATCH_SIZE, Sort.by("id")));
            if (page.isEmpty()) break;

            for (UnifiedSearchIndex index : page.getContent()) {
                try {
                    var metadata = classifier.classify(index.getTitle(), index.getContent(), index.getValidUntil());
                    index.applySemanticMetadata(metadata);
                    if (!metadata.directTopicIds().isEmpty()) classified++;
                } catch (RuntimeException e) {
                    failedIds.add(index.getId());
                    log.warn("[NoticeSemantic] classification failed. id={}, reason={}",
                            index.getId(), e.getMessage());
                }
                processed++;
            }
            repository.saveAll(page.getContent());
            repository.flush();
        }

        return new ReclassificationResult(total, processed, classified,
                processed - classified - failedIds.size(), List.copyOf(failedIds));
    }

    public record ReclassificationResult(
            long total,
            int processed,
            int classified,
            int unclassified,
            List<String> failedIds
    ) {
    }
}
