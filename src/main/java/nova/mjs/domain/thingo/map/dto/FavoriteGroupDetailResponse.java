package nova.mjs.domain.thingo.map.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 그룹 상세(05-1-1) 응답.
 *
 * 화면 상단의 그룹 헤더(이름·색상·개수)와 그 안의 장소 카드 목록을 함께 내려주어,
 * 프론트가 앞 화면에서 넘겨받은 값에 의존하지 않고 상세 화면을 자립적으로 그릴 수 있게 한다.
 * 장소가 없으면 places 는 빈 배열(프론트는 '아직 저장된 장소가 없어요' 표시).
 */
@Getter
@Builder
public class FavoriteGroupDetailResponse {

    /** 그룹 헤더 (이름·색상·타입·개수) */
    private final FavoriteGroupResponse group;
    /** 그룹에 담긴 장소 카드 목록 */
    private final List<FavoritePlaceCardResponse> places;

    public static FavoriteGroupDetailResponse of(FavoriteGroupResponse group,
                                                 List<FavoritePlaceCardResponse> places) {
        return FavoriteGroupDetailResponse.builder()
                .group(group)
                .places(places)
                .build();
    }
}
