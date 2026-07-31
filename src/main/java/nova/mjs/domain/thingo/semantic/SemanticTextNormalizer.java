package nova.mjs.domain.thingo.semantic;

import java.text.Normalizer;
import java.util.Locale;

/** 학교 공지에서 혼용되는 구분기호를 같은 별칭 비교 키로 정규화한다. */
public final class SemanticTextNormalizer {

    private SemanticTextNormalizer() {
    }

    public static String lookupKey(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[·ㆍ․・∙⋅/&()\\[\\].,_\\-\\s]", "")
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }
}
