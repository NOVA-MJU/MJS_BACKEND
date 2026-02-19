package nova.mjs.domain.thingo.ElasticSearch.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 검색 랭킹 정책 저장소.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchRankingPolicyStore {

    private static final String POLICY_PATH = "search/search_ranking_policy.json";
    private static final String RUNTIME_POLICY_PATH = "runtime/search_ranking_policy.runtime.json";

    private static final SearchRankingPolicySnapshot DEFAULT = new SearchRankingPolicySnapshot(
            3.2f,
            4.0f,
            1.8f,
            2.8f,
            SearchQueryPlan.NegativeStrategy.HARD_FILTER,
            0.2f,
            List.of(
                    new SearchQueryPlan.FreshnessRule("now-7d/d", 1.1f),
                    new SearchQueryPlan.FreshnessRule("now-30d/d", 0.6f),
                    new SearchQueryPlan.FreshnessRule("now-90d/d", 0.3f)
            ),
            List.of(
                    new SearchQueryPlan.PopularityRule("likeCount", 20, 0.6f),
                    new SearchQueryPlan.PopularityRule("commentCount", 10, 0.8f)
            )
    );

    private final ObjectMapper objectMapper;
    private SearchRankingPolicySnapshot snapshot = DEFAULT;

    @PostConstruct
    void init() {
        try {
            this.snapshot = loadFromClasspath();
            Path runtimePath = Paths.get(RUNTIME_POLICY_PATH);
            if (Files.exists(runtimePath)) {
                this.snapshot = validate(objectMapper.readValue(Files.readString(runtimePath), SearchRankingPolicySnapshot.class));
            }
        } catch (Exception e) {
            this.snapshot = DEFAULT;
            log.error("[SearchRankingPolicyStore] load failed -> fallback defaults", e);
        }
    }

    public synchronized SearchRankingPolicySnapshot snapshot() {
        return snapshot;
    }

    public synchronized SearchRankingPolicySnapshot upsert(SearchRankingPolicySnapshot requested) {
        SearchRankingPolicySnapshot validated = validate(requested);
        this.snapshot = validated;

        try {
            Path runtimePath = Paths.get(RUNTIME_POLICY_PATH);
            if (runtimePath.getParent() != null) {
                Files.createDirectories(runtimePath.getParent());
            }
            Files.writeString(runtimePath, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(validated));
        } catch (Exception e) {
            log.error("[SearchRankingPolicyStore] failed to persist runtime policy", e);
        }

        return this.snapshot;
    }

    private SearchRankingPolicySnapshot loadFromClasspath() throws Exception {
        ClassPathResource resource = new ClassPathResource(POLICY_PATH);
        if (!resource.exists()) {
            return DEFAULT;
        }

        try (InputStream is = resource.getInputStream()) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return validate(objectMapper.readValue(json, SearchRankingPolicySnapshot.class));
        }
    }

    private SearchRankingPolicySnapshot validate(SearchRankingPolicySnapshot loaded) {
        if (loaded == null) {
            return DEFAULT;
        }

        float expansionBoost = loaded.expansionTermBoost() <= 0 ? DEFAULT.expansionTermBoost() : loaded.expansionTermBoost();
        float autocompleteBoost = loaded.autocompleteBoost() <= 0 ? DEFAULT.autocompleteBoost() : loaded.autocompleteBoost();
        float noticeTypeBoost = loaded.noticeTypeBoost() <= 0 ? DEFAULT.noticeTypeBoost() : loaded.noticeTypeBoost();
        float noticeGeneralCategoryBoost = loaded.noticeGeneralCategoryBoost() <= 0 ? DEFAULT.noticeGeneralCategoryBoost() : loaded.noticeGeneralCategoryBoost();

        SearchQueryPlan.NegativeStrategy negativeStrategy = loaded.negativeStrategy() == null
                ? DEFAULT.negativeStrategy()
                : loaded.negativeStrategy();

        float negativeDownrankBoost = loaded.negativeDownrankBoost() <= 0 || loaded.negativeDownrankBoost() > 1
                ? DEFAULT.negativeDownrankBoost()
                : loaded.negativeDownrankBoost();

        List<SearchQueryPlan.FreshnessRule> freshnessRules = loaded.freshnessRules() == null || loaded.freshnessRules().isEmpty()
                ? DEFAULT.freshnessRules()
                : loaded.freshnessRules();

        List<SearchQueryPlan.PopularityRule> popularityRules = loaded.popularityRules() == null || loaded.popularityRules().isEmpty()
                ? DEFAULT.popularityRules()
                : loaded.popularityRules();

        return new SearchRankingPolicySnapshot(
                expansionBoost,
                autocompleteBoost,
                noticeTypeBoost,
                noticeGeneralCategoryBoost,
                negativeStrategy,
                negativeDownrankBoost,
                freshnessRules,
                popularityRules
        );
    }

    public record SearchRankingPolicySnapshot(
            float expansionTermBoost,
            float autocompleteBoost,
            float noticeTypeBoost,
            float noticeGeneralCategoryBoost,
            SearchQueryPlan.NegativeStrategy negativeStrategy,
            float negativeDownrankBoost,
            List<SearchQueryPlan.FreshnessRule> freshnessRules,
            List<SearchQueryPlan.PopularityRule> popularityRules
    ) {}
}
