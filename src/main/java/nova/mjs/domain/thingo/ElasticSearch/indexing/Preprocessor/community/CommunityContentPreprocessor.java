package nova.mjs.domain.thingo.ElasticSearch.indexing.Preprocessor.community;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class CommunityContentPreprocessor {

    /**
     * Community Editor JSON → 검색용 텍스트
     *
     * 규칙:
     * - block 단위 순서 유지
     * - content[].text 만 추출
     * - 스타일/props/type 전부 무시
     */
    public String normalize(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return "";
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);

            StringBuilder result = new StringBuilder();

            for (JsonNode block : root) {
                JsonNode contentArray = block.get("content");
                if (contentArray == null || !contentArray.isArray()) {
                    continue;
                }

                for (JsonNode content : contentArray) {
                    JsonNode textNode = content.get("text");
                    if (textNode != null && !textNode.asText().isBlank()) {
                        result.append(textNode.asText()).append(" ");
                    }
                }
            }

            return result.toString().trim();

        } catch (Exception e) {
            // JSON 파싱 실패 시 원문 그대로 저장 (검색 누락 방지)
            return rawJson;
        }
    }
}
