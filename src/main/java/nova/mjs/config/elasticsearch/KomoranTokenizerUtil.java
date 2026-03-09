package nova.mjs.config.elasticsearch;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.Token;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class KomoranTokenizerUtil {

    private static final Komoran komoran;

    static {
        komoran = new Komoran(DEFAULT_MODEL.FULL);
        String dicPath = Objects.requireNonNull(
                        KomoranTokenizerUtil.class.getClassLoader()
                                .getResource("komoran_user_dic.txt"))
                .getFile();
        komoran.setUserDic(dicPath);
    }

    private static final Set<String> STOPWORDS = Set.of(
            "은", "는", "에서", "으로", "하고", "이다", "하는", "을", "를", "의", "이", "가", "과", "와", "로", "하다"
    );

    private KomoranTokenizerUtil() {
    }

    public static List<String> generateSuggestions(String text) {
        List<String> units = extractSuggestionUnits(text);
        if (units.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>(units);
        result.addAll(buildAdjacentNgrams(units));

        return result.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    public static String buildSearchTokens(String... texts) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();

        for (String text : texts) {
            if (text == null || text.isBlank()) {
                continue;
            }

            String normalized = normalizeSearchText(text);
            String compact = compact(text);

            if (!normalized.isBlank()) {
                tokens.add(normalized);
            }
            if (!compact.isBlank()) {
                tokens.add(compact);
            }

            List<String> units = extractSuggestionUnits(text);
            tokens.addAll(units);
            tokens.addAll(buildAdjacentNgrams(units));
            tokens.addAll(buildSkippedCompounds(units));
        }

        return String.join(" ", tokens);
    }

    public static String normalizeSearchText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    public static String compact(String text) {
        return normalizeSearchText(text).replace(" ", "");
    }

    public static List<String> extractQueryTerms(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        LinkedHashSet<String> terms = new LinkedHashSet<>();
        String normalized = normalizeSearchText(text);
        String compact = compact(text);

        if (!compact.isBlank()) {
            terms.add(compact);
        }

        for (String token : normalized.split("\\s+")) {
            String trimmed = token.trim();
            if (trimmed.length() >= 2) {
                terms.add(trimmed);
            }
        }

        terms.addAll(extractSuggestionUnits(text));

        return terms.stream()
                .map(String::trim)
                .filter(value -> value.length() >= 2)
                .distinct()
                .toList();
    }

    private static List<String> extractSuggestionUnits(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<Token> tokens = komoran.analyze(text).getTokenList();
        List<String> units = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (Token token : tokens) {
            String morph = token.getMorph();
            String pos = token.getPos();

            if (isYearOrOrdinal(morph)) {
                units.add(morph);
                continue;
            }

            if ((pos.startsWith("NN") || pos.equals("SL"))
                    && morph.length() >= 2
                    && !STOPWORDS.contains(morph)) {
                current.append(morph);
            } else if (current.length() > 0) {
                units.add(current.toString());
                current.setLength(0);
            }
        }

        if (current.length() > 0) {
            units.add(current.toString());
        }

        return units.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private static List<String> buildAdjacentNgrams(List<String> units) {
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i < units.size() - 1; i++) {
            String first = units.get(i);
            String second = units.get(i + 1);
            ngrams.add(first + " " + second);
            ngrams.add(first + second);
        }
        return ngrams;
    }

    private static List<String> buildSkippedCompounds(List<String> units) {
        if (units.size() < 3) {
            return List.of();
        }

        List<String> compounds = new ArrayList<>();
        int limit = Math.min(units.size(), 6);
        for (int i = 0; i < limit; i++) {
            for (int j = i + 2; j < limit; j++) {
                compounds.add(units.get(i) + units.get(j));
                compounds.add(units.get(i) + " " + units.get(j));
            }
        }
        return compounds;
    }

    private static boolean isYearOrOrdinal(String token) {
        return token.matches("^\\d{4}년$") || token.matches("^제\\d+차$");
    }
}
