package nova.mjs.domain.thingo.map.dto;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.map.entity.FavoriteGroup;
import nova.mjs.domain.thingo.map.entity.FavoriteGroupColor;
import nova.mjs.domain.thingo.map.entity.FavoriteGroupType;

/**
 * 즐겨찾기 그룹 카드 1개 (그룹 리스트 / 그룹 상세 헤더 공용).
 *
 * 별(즐겨찾기) 아이콘은 color 로 시각적 구분만 한다(기능 없음).
 * placeCount 는 그룹에 담긴 장소 수(핀 그룹) 또는 버스 즐겨찾기 수('버스' 그룹).
 */
@Getter
@Builder
public class FavoriteGroupResponse {

    /** 그룹 ID */
    private final Long id;
    /** 그룹명 (최대 12자) */
    private final String name;
    /** 그룹 색상 (팔레트 enum 이름) */
    private final String color;
    /** 그룹 종류 (SYSTEM_MY_PLACES / SYSTEM_BUS / USER) */
    private final String type;
    /** 시스템 기본 그룹 여부 (상단 고정·수정삭제 불가 판단) */
    private final boolean system;
    /** 저장된 장소 개수 (없으면 0) */
    private final long placeCount;

    public static FavoriteGroupResponse of(FavoriteGroup group, long placeCount) {
        return FavoriteGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .color(group.getColor().name())
                .type(group.getType().name())
                .system(group.isSystem())
                .placeCount(placeCount)
                .build();
    }

    /**
     * 가상 '버스' 그룹 카드. DB에 저장되지 않으며(핀이 아니라 정류장·노선 단위),
     * 그룹 리스트 응답에만 상단 고정으로 끼워 넣는다.
     * id 는 null 이고, 프론트는 type=SYSTEM_BUS 로 식별해 버스 화면으로 이동한다.
     * placeCount 는 회원이 담은 버스 노선 총합(정류장 A/B 무관).
     */
    public static FavoriteGroupResponse virtualBus(long routeCount) {
        return FavoriteGroupResponse.builder()
                .id(null)
                .name("버스")
                .color(FavoriteGroupColor.AMBER.name())
                .type(FavoriteGroupType.SYSTEM_BUS.name())
                .system(true)
                .placeCount(routeCount)
                .build();
    }
}
