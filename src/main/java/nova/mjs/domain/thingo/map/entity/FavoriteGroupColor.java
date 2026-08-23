package nova.mjs.domain.thingo.map.entity;

/**
 * 즐겨찾기 그룹 색상 팔레트.
 *
 * 게시판 글쓰기(노션형) 컬러 팔레트와 동일한 10색 구성이며, 백엔드는 색 이름(enum)만 저장한다.
 * 실제 표시용 hex 값은 프론트/공용 상수에서 이 이름과 1:1로 매핑한다.
 * 기본값은 {@link #BLUE}(띵고 색).
 */
public enum FavoriteGroupColor {
    CORAL,
    RED,
    ORANGE,
    AMBER,
    LIME,
    GREEN,
    SKY,
    BLUE,   // 기본값 (띵고 색)
    PURPLE,
    GRAY;

    /** 미지정 시 사용할 기본 색상 */
    public static final FavoriteGroupColor DEFAULT = BLUE;
}
