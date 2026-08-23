package nova.mjs.domain.thingo.map.entity;

/**
 * 즐겨찾기 그룹 종류.
 *
 * - SYSTEM_MY_PLACES: 시스템 기본 '내 장소' 그룹. 회원별 1개(DB 저장). 수정/삭제 불가. 장소(핀) 즐겨찾기의 기본 편입 그룹.
 * - SYSTEM_BUS: '버스' 가상 그룹의 응답 식별용 마커. DB에 저장되지 않으며(BusFavorite 재사용),
 *   그룹 리스트 응답에만 가상 항목으로 노출된다. 프론트는 이 타입으로 버스 화면 라우팅을 판단한다.
 * - USER: 사용자가 직접 만든 그룹. 이름/색상 수정 및 삭제 가능.
 *
 * 시스템 그룹('내 장소' → '버스')은 정렬과 무관하게 목록 상단에 고정된다.
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
