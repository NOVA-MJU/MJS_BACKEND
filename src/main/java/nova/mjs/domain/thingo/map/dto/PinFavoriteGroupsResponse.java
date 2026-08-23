package nova.mjs.domain.thingo.map.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * '그룹 선택 바텀시트' 조회 응답.
 *
 * 특정 장소(핀)를 즐겨찾기에 담을 때 뜨는 바텀시트용.
 * 상단에 장소명·메모(기존 값)를 채우고, 회원의 그룹 목록을 각 그룹의 선택 여부(selected)와 함께 내려준다.
 * 그룹은 시스템 그룹('내 장소','버스') 상단 고정 후 최신순으로 정렬한다.
 */
@Getter
@Builder
public class PinFavoriteGroupsResponse {

    /** 대상 핀 ID */
    private final Long pinId;
    /** 장소명 (바텀시트 제목) */
    private final String placeName;
    /** 기존 메모(프리필). 없으면 null */
    private final String memo;
    /** 선택 가능한 그룹 목록 */
    private final List<GroupSelection> groups;

    @Getter
    @Builder
    public static class GroupSelection {
        private final Long id;
        private final String name;
        private final String color;
        private final String type;
        private final boolean system;
        private final long placeCount;
        /** 이 핀이 해당 그룹에 이미 담겨 있는지 */
        private final boolean selected;
    }

    public static PinFavoriteGroupsResponse of(Long pinId, String placeName, String memo,
                                               List<GroupSelection> groups) {
        return PinFavoriteGroupsResponse.builder()
                .pinId(pinId)
                .placeName(placeName)
                .memo(memo)
                .groups(groups)
                .build();
    }
}
