package nova.mjs.domain.thingo.map.support;

import nova.mjs.domain.thingo.map.entity.Pin;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자 행동 데이터가 충분하지 않은 대동명지도용 콜드 스타트 추천 정렬기.
 *
 * <p>장소를 약 250m 공간 구역으로 묶고 모든 구역에서 한 곳씩 무작위로 선택한 다음,
 * 두 번째 순회를 시작한다. 따라서 밀집 상권의 장소 수가 많아도 첫 페이지를 독점하지
 * 못한다. 사용자(또는 클라이언트 seed)와 날짜를 사용한 결정적 난수로 무한 스크롤
 * 중 순서와 페이지 경계를 안정적으로 유지한다.</p>
 */
@Component
public class MapRecommendationRanker {

    static final double SPATIAL_BUCKET_METERS = 250.0;
    private static final double METERS_PER_LATITUDE_DEGREE = 111_320.0;

    public List<Pin> rank(List<Pin> pins, String categoryCode, String viewerSeed, LocalDate rankingDate) {
        if (pins.size() < 2) {
            return List.copyOf(pins);
        }

        long seed = stableHash(categoryCode + "|" + viewerSeed + "|" + rankingDate);
        Map<SpatialBucket, List<Pin>> grouped = new LinkedHashMap<>();

        for (Pin pin : pins) {
            grouped.computeIfAbsent(spatialBucket(pin), ignored -> new ArrayList<>()).add(pin);
        }

        grouped.values().forEach(bucket -> bucket.sort(
                Comparator.comparingDouble(pin -> randomScore(seed, "pin", pinIdentity(pin)))));

        List<SpatialBucket> bucketOrder = new ArrayList<>(grouped.keySet());
        bucketOrder.sort(Comparator.comparingDouble(
                bucket -> randomScore(seed, "bucket", bucket.identity())));

        List<Pin> ranked = new ArrayList<>(pins.size());
        String previousCategory = null;
        int round = 0;

        while (ranked.size() < pins.size()) {
            int currentRound = round;
            List<SpatialBucket> activeBuckets = bucketOrder.stream()
                    .filter(bucket -> !grouped.get(bucket).isEmpty())
                    .sorted(Comparator.comparingDouble(
                            bucket -> randomScore(seed ^ mix64(currentRound), "round", bucket.identity())))
                    .toList();

            for (SpatialBucket bucket : activeBuckets) {
                List<Pin> candidates = grouped.get(bucket);
                Pin selected = removeRandomCandidateAvoidingCategoryRepeat(candidates, previousCategory);
                ranked.add(selected);
                previousCategory = categoryCode(selected);
            }
            round++;
        }

        return ranked;
    }

    private Pin removeRandomCandidateAvoidingCategoryRepeat(List<Pin> candidates, String previousCategory) {
        if (previousCategory != null) {
            for (int i = 0; i < candidates.size(); i++) {
                if (!previousCategory.equals(categoryCode(candidates.get(i)))) {
                    return candidates.remove(i);
                }
            }
        }
        return candidates.remove(0);
    }

    private SpatialBucket spatialBucket(Pin pin) {
        Double latitude = pin.resolveLatitude();
        Double longitude = pin.resolveLongitude();
        if (latitude == null || longitude == null) {
            return SpatialBucket.unknown(pinIdentity(pin));
        }

        long latitudeCell = (long) Math.floor(
                latitude * METERS_PER_LATITUDE_DEGREE / SPATIAL_BUCKET_METERS);
        double metersPerLongitudeDegree = METERS_PER_LATITUDE_DEGREE * Math.cos(Math.toRadians(latitude));
        long longitudeCell = (long) Math.floor(
                longitude * metersPerLongitudeDegree / SPATIAL_BUCKET_METERS);
        return SpatialBucket.located(latitudeCell, longitudeCell);
    }

    private String categoryCode(Pin pin) {
        return pin.getCategory().getCode();
    }

    private String pinIdentity(Pin pin) {
        return pin.getId() != null ? Long.toString(pin.getId()) : pin.getCode();
    }

    private double randomScore(long seed, String namespace, String identity) {
        return toUnitDouble(mix64(seed ^ stableHash(namespace + "|" + identity)));
    }

    private long stableHash(String value) {
        long hash = -3750763034362895579L;
        for (int i = 0; i < value.length(); i++) {
            hash ^= value.charAt(i);
            hash *= 1099511628211L;
        }
        return hash;
    }

    private long mix64(long value) {
        value = (value ^ (value >>> 33)) * 0xff51afd7ed558ccdL;
        value = (value ^ (value >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return value ^ (value >>> 33);
    }

    private double toUnitDouble(long value) {
        return (value >>> 11) * 0x1.0p-53;
    }

    private record SpatialBucket(long latitudeCell, long longitudeCell, String unknownIdentity) {

        private static SpatialBucket located(long latitudeCell, long longitudeCell) {
            return new SpatialBucket(latitudeCell, longitudeCell, "");
        }

        private static SpatialBucket unknown(String pinIdentity) {
            return new SpatialBucket(0, 0, pinIdentity);
        }

        private String identity() {
            return latitudeCell + "|" + longitudeCell + "|" + unknownIdentity;
        }
    }
}
