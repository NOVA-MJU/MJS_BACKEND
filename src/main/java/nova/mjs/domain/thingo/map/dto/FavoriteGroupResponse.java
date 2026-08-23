package nova.mjs.domain.thingo.map.dto;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.map.entity.FavoriteGroup;

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
}
