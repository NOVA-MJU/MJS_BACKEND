package nova.mjs.domain.thingo.ElasticSearch.DTO;

import java.util.List;

public record SeasonalSuggestionResponse(
        String period,
        List<String> keywords
) {
}
