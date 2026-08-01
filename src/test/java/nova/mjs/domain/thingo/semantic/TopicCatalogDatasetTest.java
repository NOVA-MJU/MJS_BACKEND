package nova.mjs.domain.thingo.semantic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TopicCatalogDatasetTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TopicCatalog topicCatalog = new TopicCatalog();

    @Test
    @DisplayName("축약어·띄어쓰기·구두점 정규화 평가셋을 모두 통과한다")
    void resolvesAliasNormalizationDataset() throws Exception {
        try (var input = getClass().getResourceAsStream(
                "/semantic/alias-normalization-eval-v1.jsonl")) {
            assertThat(input).isNotNull();
            try (var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    JsonNode row = objectMapper.readTree(line);
                    Set<String> expected = new LinkedHashSet<>();
                    row.path("expectedDirectTopicIds").forEach(node -> expected.add(node.asText()));
                    Set<String> actual = new LinkedHashSet<>();
                    topicCatalog.resolve(row.path("input").asText()).forEach(
                            resolved -> actual.add(resolved.topic().topicId()));

                    assertThat(actual)
                            .as(row.path("caseId").asText() + ": " + row.path("note").asText())
                            .containsExactlyInAnyOrderElementsOf(expected);
                }
            }
        }
    }
}
