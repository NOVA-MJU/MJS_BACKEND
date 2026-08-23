package nova.mjs.domain.thingo.map.entity;

/**
 * 즐겨찾기 그룹 종류.
 *
 * - SYSTEM_MY_PLACES: 시스템 기본 '내 장소' 그룹. 회원별 1개. 수정/삭제 불가. 장소(핀) 즐겨찾기의 기본 편입 그룹.
 * - SYSTEM_BUS: 시스템 기본 '버스' 그룹. 회원별 1개. 수정/삭제 불가. 내용물은 BusFavorite에서 조회.
 * - USER: 사용자가 직접 만든 그룹. 이름/색상 수정 및 삭제 가능.
 *
 * 시스템 그룹은 정렬과 무관하게 목록 상단에 고정된다.
 */
public enum FavoriteGroupType {
    SYSTEM_MY_PLACES,
    SYSTEM_BUS,
    USER;

    /** 시스템 기본 제공 그룹인지 여부 (수정/삭제 차단 판단용) */
    public boolean isSystem() {
        return this == SYSTEM_MY_PLACES || this == SYSTEM_BUS;
    }
}
