package nova.mjs.domain.thingo.map.support;

import nova.mjs.domain.thingo.map.entity.Category;
import nova.mjs.domain.thingo.map.entity.Pin;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class MapRecommendationRankerTest {

    private final DistanceCalculator distanceCalculator = new DistanceCalculator();
    private final MapRecommendationRanker ranker = new MapRecommendationRanker();

    @Test
    void sameSeedAndDateProduceStableOrder() {
        List<Pin> pins = List.of(
                pin(1L, "a", "daedong-kr", 37.5800, 126.9200),
                pin(2L, "b", "daedong-kr", 37.5810, 126.9210),
                pin(3L, "c", "daedong-kr", 37.5820, 126.9220)
        );

        List<Long> first = ids(ranker.rank(
                pins, "daedong-kr", "viewer-1", LocalDate.of(2026, 7, 30)));
        List<Long> second = ids(ranker.rank(
                pins, "daedong-kr", "viewer-1", LocalDate.of(2026, 7, 30)));

        assertThat(first).containsExactlyElementsOf(second);
    }

    @Test
    void everySpatialBucketIsPickedOnceBeforeAnyBucketRepeats() {
        List<Pin> pins = new ArrayList<>();
        for (int area = 0; area < 4; area++) {
            for (int item = 0; item < 2; item++) {
                long id = area * 10L + item + 1;
                pins.add(pin(
                        id,
                        "area" + area + "-" + item,
                        "daedong-" + item,
                        37.574 + area * 0.004,
                        126.915 + area * 0.004));
            }
        }

        List<Pin> ranked = ranker.rank(
                pins, "daedong", "viewer-1", LocalDate.of(2026, 7, 30));

        assertThat(ranked.subList(0, 4))
                .extracting(pin -> pin.getCode().substring(0, 5))
                .doesNotHaveDuplicates()
                .hasSize(4);
    }

    @Test
    void spatiallyClosePlacesAreNotSelectedConsecutivelyWhenFarCandidateExists() {
        List<Pin> pins = List.of(
                pin(1L, "near-a", "daedong-cafe", 37.5800, 126.9200),
                pin(2L, "near-b", "daedong-cafe", 37.5801, 126.9201),
                pin(3L, "far", "daedong-cafe", 37.5850, 126.9250)
        );

        List<Pin> ranked = ranker.rank(
                pins, "daedong-cafe", "viewer-1", LocalDate.of(2026, 7, 30));

        double firstGap = distanceCalculator.distanceMeters(
                ranked.get(0).resolveLatitude(), ranked.get(0).resolveLongitude(),
                ranked.get(1).resolveLatitude(), ranked.get(1).resolveLongitude());
        assertThat(firstGap).isGreaterThan(250.0);
    }

    private List<Long> ids(List<Pin> pins) {
        return pins.stream().map(Pin::getId).toList();
    }

    private Pin pin(Long id, String code, String categoryCode, double latitude, double longitude) {
        Category category = mock(Category.class);
        given(category.getCode()).willReturn(categoryCode);

        Pin pin = mock(Pin.class);
        given(pin.getId()).willReturn(id);
        given(pin.getCode()).willReturn(code);
        given(pin.getCategory()).willReturn(category);
        given(pin.resolveLatitude()).willReturn(latitude);
        given(pin.resolveLongitude()).willReturn(longitude);
        return pin;
    }
}
