package nova.mjs.domain.mentorship.ElasticSearch.EventSynchronization;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SearchContentPreprocessor {

    /** 검색에 의미 있다고 판단하는 최소 길이 */
    private static final int MIN_MEANINGFUL_LENGTH = 30;

    /**
     * HTML → 검색용 텍스트 정규화
     *
     * 처리 단계
     * 1. HTML 태그 제거
     * 2. 이미지 alt 텍스트 병합
     * 3. 공백 정규화
     * 4. 의미 없는 짧은 문장 제거
     */
    public String normalize(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }

        Document doc = Jsoup.parse(html);

        // 본문 텍스트
        String text = doc.text();

        // 이미지 alt 텍스트
        String imageAltText = doc.select("img[alt]")
                .eachAttr("alt")
                .stream()
                .collect(Collectors.joining(" "));

        String merged = (text + " " + imageAltText)
                .replaceAll("\\s+", " ")
                .trim();

        // null 반환 금지
        if (merged.length() < MIN_MEANINGFUL_LENGTH) {
            return merged; // 짧아도 유지
        }

        return merged;
    }
}
