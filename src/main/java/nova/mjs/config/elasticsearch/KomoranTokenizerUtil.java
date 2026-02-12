package nova.mjs.config.elasticsearch;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.Token;
import lombok.extern.log4j.Log4j2;

import java.util.*;

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

    // 불용어 정의
    private static final Set<String> stopwords = Set.of(
            "의", "및", "에서", "으로", "하고", "이다", "되는", "〈", "〉", "를", "은", "는", "이", "가", "과", "도", "에", "로", "되다"
    );

    /**
     * suggest 필드에 넣을 키워드 후보 리스트 생성
     */
    public static List<String> generateSuggestions(String text) {

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

            if ((pos.startsWith("NN") || pos.equals("SL")) &&
                    morph.length() >= 2 &&
                    !stopwords.contains(morph)) {

                current.append(morph);
            } else {
                if (current.length() > 0) {
                    units.add(current.toString());
                    current.setLength(0);
                }
            }
        }

        if (current.length() > 0) {
            units.add(current.toString());
        }

        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i < units.size() - 1; i++) {
            ngrams.add(units.get(i) + " " + units.get(i + 1));
        }

        List<String> result = new ArrayList<>(units);
        result.addAll(ngrams);

        return result;
    }

    private static boolean isYearOrOrdinal(String token) {
        return token.matches("^\\d{4}년$") || token.matches("^제\\d+회$");
    }
}