package nova.mjs.domain.thingo.map.dto;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.map.entity.Pin;
import org.springframework.web.util.UriComponentsBuilder;

/** 정확한 실내 title 검색 결과. 프론트는 link로 그대로 이동한다. */
@Getter
@Builder
public class FloorMapSearchItemResponse {

    private final Long id;
    private final String name;
    private final String link;

    public static FloorMapSearchItemResponse from(Pin pin) {
        if (pin == null || !pin.isInsideBuilding() || pin.getFloor() == null
                || pin.getIndoorCode() == null || pin.getIndoorCode().isBlank()) {
            throw new IllegalArgumentException("층별 안내도 검색 결과를 만들 수 없는 핀입니다.");
        }

        String link = UriComponentsBuilder.fromPath("/maps/floor")
                .queryParam("buildingId", pin.getParentBuilding().getId())
                .queryParam("floorLabel", pin.getFloor().getLabel())
                .queryParam("target", pin.getIndoorCode())
                .encode()
                .toUriString();

        return FloorMapSearchItemResponse.builder()
                .id(pin.getId())
                .name(pin.getName())
                .link(link)
                .build();
    }
}
