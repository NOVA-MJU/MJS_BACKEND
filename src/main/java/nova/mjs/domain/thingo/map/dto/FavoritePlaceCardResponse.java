package nova.mjs.domain.thingo.map.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 그룹 상세의 장소 카드 1개.
 *
 * 지도 목록 카드(PinSummaryResponse)와 동일한 표시 필드에 그룹 메모를 더한 형태.
 * favorite 는 항상 true(그룹에 담겨 있으므로)이며, 별 아이콘 해제 시 해당 그룹에서 제거된다.
 */
@Getter
@Builder
public class FavoritePlaceCardResponse {

    /** 핀 ID */
    private final Long pinId;
    /** 종류 (BUILDING / PLACE) */
    private final String type;
    /** 건물명/장소명 */
    private final String name;
    /** 카테고리 코드 */
    private final String categoryCode;
    /** 카테고리 아이콘 키 (프린트/카페 등) */
    private final String iconKey;
    /** 이미지 URL. 없으면 null */
    private final String imageUrl;
    /** 예시 강의실 코드 (건물만). 없으면 null */
    private final String classroomCode;
    /** 위치 텍스트 (장소만). 없으면 null */
    private final String location;
    /** 운영 상태 라벨 (예: "운영중"). 없으면 null */
    private final String operatingStatus;
    /** 현재 위치로부터 거리(m). GPS/캠퍼스 밖이면 null */
    private final Integer distanceMeters;
    /** 지도 마커용 위도 */
    private final Double latitude;
    /** 지도 마커용 경도 */
    private final Double longitude;
    /** 이 그룹에서의 메모. 없으면 null */
    private final String memo;
    /** 즐겨찾기 여부 (그룹 내 카드는 항상 true) */
    private final boolean favorite;

    public static FavoritePlaceCardResponse of(PinSummaryResponse s, String memo) {
        return FavoritePlaceCardResponse.builder()
                .pinId(s.getId())
                .type(s.getType())
                .name(s.getName())
                .categoryCode(s.getCategoryCode())
                .iconKey(s.getIconKey())
                .imageUrl(s.getImageUrl())
                .classroomCode(s.getClassroomCode())
                .location(s.getLocation())
                .operatingStatus(s.getOperatingStatus())
                .distanceMeters(s.getDistanceMeters())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .memo(memo)
                .favorite(true)
                .build();
    }
}
