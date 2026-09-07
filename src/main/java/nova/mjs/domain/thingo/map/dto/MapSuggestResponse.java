package nova.mjs.domain.thingo.map.dto;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.map.entity.Pin;
import nova.mjs.domain.thingo.map.support.MapSearchRouteResolver;

/**
 * 검색 자동완성 항목 1개.
 *
 * 검색 결과 카드(PinSummaryResponse)와 달리 거리·운영상태를 계산하지 않는 경량 응답이다.
 * 아이콘·종류·ID를 함께 내려 프론트가 드롭다운에 아이콘을 표시하고, 탭 시 바로 상세로 이동할 수 있게 한다.
 */
@Getter
@Builder(toBuilder = true)
public class MapSuggestResponse {

    /** 핀 ID (탭 시 상세 요청에 사용) */
    private final Long id;
    /** 건물명/장소명 (자동완성에 표시) */
    private final String name;
    /** 항목별 이동 종류 (BUILDING / PLACE / FLOOR_MAP) */
    private final String type;
    /** 소속 카테고리 코드 */
    private final String categoryCode;
    /** 카드 아이콘 키 (카테고리 아이콘) */
    private final String iconKey;
    /** 실제 호실/요소 코드 (예: S1353). 없으면 null */
    private final String indoorCode;
    /** FLOOR_MAP 항목의 층별안내도 상대 URL. 그 외 항목은 null */
    private final String link;

    public static MapSuggestResponse from(Pin pin) {
        return MapSuggestResponse.builder()
                .id(pin.getId())
                .name(pin.getName())
                .type(MapSearchRouteResolver.responseType(pin))
                .categoryCode(pin.getCategory().getCode())
                .iconKey(pin.getCategory().getIconKey())
                .indoorCode(pin.getIndoorCode())
                .link(MapSearchRouteResolver.link(pin))
                .build();
    }

    /**
     * 층별안내도 라우팅(floorMap)을 요청하지 않았을 때 FLOOR_MAP 항목을 기본 PLACE로 낮춘다.
     * 종류만 되돌리고 층별안내도 link는 제거한다. FLOOR_MAP이 아니면 그대로 반환한다.
     */
    public MapSuggestResponse toPlaceFallback() {
        if (!MapSearchRouteResolver.FLOOR_MAP_TYPE.equals(type)) {
            return this;
        }
        return toBuilder()
                .type("PLACE")
                .link(null)
                .build();
    }
}
