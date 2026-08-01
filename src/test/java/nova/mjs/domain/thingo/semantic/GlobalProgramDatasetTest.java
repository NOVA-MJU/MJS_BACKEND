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

class GlobalProgramDatasetTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NoticeSemanticClassifier classifier = new NoticeSemanticClassifier(new TopicCatalog());

    @Test
    @DisplayName("실제 해외 프로그램명과 오탐 경계 평가셋을 통과한다")
    void classifiesActualGlobalProgramDataset() throws Exception {
        try (var input = getClass().getResourceAsStream("/semantic/global-program-cases-v1.jsonl")) {
            assertThat(input).isNotNull();
            try (var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    JsonNode row = objectMapper.readTree(line);
                    Set<String> expected = new LinkedHashSet<>();
                    row.path("expectedDirectTopicIds").forEach(node -> expected.add(node.asText()));
                    NoticeSemanticMetadata actual = classifier.classify(
                            row.path("title").asText(), row.path("content").asText(), null);

                    assertThat(actual.directTopicIds())
                            .as(row.path("caseId").asText() + " topics: " + row.path("note").asText())
                            .containsExactlyInAnyOrderElementsOf(expected);
                    assertThat(actual.eventType().name())
                            .as(row.path("caseId").asText() + " event: " + row.path("note").asText())
                            .isEqualTo(row.path("expectedEventType").asText());
                }
            }
        }
    }
}
