package nova.mjs.domain.thingo.map.dto;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.map.entity.Pin;
import nova.mjs.domain.thingo.map.entity.PinType;

/**
 * 지도 전용 마커 응답. 목록 카드와 분리하여 내부 시설을 건물별로 묶어 표현한다.
 */
@Getter
@Builder
public class MapMarkerResponse {

    /** 클릭 시 상세 조회에 사용할 핀 ID. 묶음 마커는 부모 건물 ID다. */
    private final Long id;
    /** BUILDING / PLACE */
    private final String type;
    /** 마커 제목. 예: 동아리방 36개 */
    private final String title;
    /** 보조 위치. 예: 학생회관 */
    private final String location;
    private final String categoryCode;
    private final String iconKey;
    /** 마커가 대표하는 시설 수. 일반 장소·건물은 1이다. */
    private final int count;
    private final boolean grouped;
    private final Double latitude;
    private final Double longitude;

    public static MapMarkerResponse grouped(Pin building, String categoryCode, String categoryLabel,
                                            String iconKey, int count) {
        return MapMarkerResponse.builder()
                .id(building.getId())
                .type("BUILDING")
                .title(categoryLabel + " " + count + "개")
                .location(building.getName())
                .categoryCode(categoryCode)
                .iconKey(iconKey)
                .count(count)
                .grouped(true)
                .latitude(building.getLatitude())
                .longitude(building.getLongitude())
                .build();
    }

    public static MapMarkerResponse single(Pin pin) {
        return MapMarkerResponse.builder()
                .id(pin.getId())
                .type(pin.getType().name())
                .title(pin.getName())
                .location(pin.getType() == PinType.PLACE ? pin.getAddress() : null)
                .categoryCode(pin.getCategory().getCode())
                .iconKey(pin.getCategory().getIconKey())
                .count(1)
                .grouped(false)
                .latitude(pin.resolveLatitude())
                .longitude(pin.resolveLongitude())
                .build();
    }
}
