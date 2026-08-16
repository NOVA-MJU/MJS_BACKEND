package nova.mjs.domain.thingo.map.support;

import nova.mjs.domain.thingo.map.entity.Pin;
import nova.mjs.domain.thingo.map.entity.PinType;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 지도 검색 항목의 프론트 이동 방식과 층별안내도 링크를 한 곳에서 결정한다.
 *
 * 엔티티의 PinType은 BUILDING/PLACE 도메인 구분을 유지한다. 검색 응답에서만
 * 상위 건물과 층이 모두 있는 PLACE를 FLOOR_MAP으로 표현한다.
 */
public final class MapSearchRouteResolver {

    public static final String FLOOR_MAP_TYPE = "FLOOR_MAP";

    private MapSearchRouteResolver() {
    }

    public static String responseType(Pin pin) {
        return isFloorMapTarget(pin) ? FLOOR_MAP_TYPE : pin.getType().name();
    }

    public static String link(Pin pin) {
        if (!isFloorMapTarget(pin)) {
            return null;
        }

        return UriComponentsBuilder.fromPath("/maps/floor")
                .queryParam("buildingId", pin.getParentBuilding().getId())
                .queryParam("floorLabel", pin.getFloor().getLabel())
                .queryParam("target", pin.getCode())
                .build()
                .encode()
                .toUriString();
    }

    public static boolean isFloorMapTarget(Pin pin) {
        return pin.getType() == PinType.PLACE
                && pin.getParentBuilding() != null
                && pin.getFloor() != null;
    }
}
