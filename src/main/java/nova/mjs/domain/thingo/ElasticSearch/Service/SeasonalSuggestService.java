package nova.mjs.domain.thingo.ElasticSearch.Service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.DTO.SeasonalSuggestionResponse;
import nova.mjs.domain.thingo.ElasticSearch.Entity.PopularSearchKeyword;
import nova.mjs.domain.thingo.ElasticSearch.Repository.PopularSearchKeywordRepository;
import nova.mjs.domain.thingo.realtimeKeyword.RealtimeKeywordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeasonalSuggestService {

    private static final int DEFAULT_LIMIT = 5;

    private final PopularSearchKeywordRepository popularSearchKeywordRepository;
    private final RealtimeKeywordService realtimeKeywordService;

    public SeasonalSuggestionResponse getSeasonalSuggestions(Integer month) {
        int targetMonth = (month == null) ? LocalDate.now().getMonthValue() : month;

        if (targetMonth < 1 || targetMonth > 12) {
            throw new IllegalArgumentException("month는 1~12 사이여야 합니다.");
        }

        if (isOffMonth(targetMonth)) {
            return new SeasonalSuggestionResponse("인기 검색어", realtimeKeywordService.getTopKeywords(DEFAULT_LIMIT));
        }

        PopularSearchKeyword.SeasonalPeriod period = resolvePeriod(targetMonth);

        List<String> keywords = popularSearchKeywordRepository
                .findTop5ByPeriodOrderByDisplayOrderAscIdAsc(period)
                .stream()
                .map(PopularSearchKeyword::getKeyword)
                .toList();

        return new SeasonalSuggestionResponse(toLabel(period), keywords);
    }

    private boolean isOffMonth(int month) {
        return month == 5 || month == 11;
    }

    private PopularSearchKeyword.SeasonalPeriod resolvePeriod(int month) {
        return switch (month) {
            case 3, 9 -> PopularSearchKeyword.SeasonalPeriod.ENROLLMENT;
            case 4, 10 -> PopularSearchKeyword.SeasonalPeriod.EXAM;
            case 6, 12 -> PopularSearchKeyword.SeasonalPeriod.SEMESTER_END;
            case 1, 2, 7, 8 -> PopularSearchKeyword.SeasonalPeriod.VACATION;
            default -> throw new IllegalStateException("정의되지 않은 월입니다: " + month);
        };
    }

    private String toLabel(PopularSearchKeyword.SeasonalPeriod period) {
        return switch (period) {
            case ENROLLMENT -> "개강";
            case EXAM -> "시험";
            case SEMESTER_END -> "종강";
            case VACATION -> "방학";
        };
    }
}
