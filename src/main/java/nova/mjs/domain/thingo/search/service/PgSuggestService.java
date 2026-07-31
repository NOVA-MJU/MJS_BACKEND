package nova.mjs.domain.thingo.search.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.search.suggest.CoreSearchSuggestionCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PgSuggestService {

    private static final int DEFAULT_LIMIT = 10;

    private final CoreSearchSuggestionCatalog suggestionCatalog;

    public List<String> getSuggestions(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return suggestionCatalog.suggest(keyword.trim(), DEFAULT_LIMIT);
    }
}
