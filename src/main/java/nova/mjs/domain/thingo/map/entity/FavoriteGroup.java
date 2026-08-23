package nova.mjs.domain.thingo.map.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.util.entity.BaseEntity;

/**
 * 즐겨찾기 그룹(폴더).
 *
 * 한 회원이 장소(핀)를 분류해 담는 단위. 그룹명 + 색상을 가진다.
 * '내 장소'(SYSTEM_MY_PLACES)는 회원별로 시스템이 기본 제공하는 저장 그룹으로,
 * 수정/삭제할 수 없고 정렬과 무관하게 목록 최상단에 고정된다.
 * ('버스'는 핀이 아니라 정류장·노선 단위라 이 엔티티로 저장하지 않고, 그룹 리스트 응답에만
 *  가상 항목으로 '내 장소' 다음에 노출된다 — FavoriteGroupResponse.virtualBus 참고.)
 *
 * [정렬 기준]
 * - 최신순: createdAt DESC
 * - 가나다순: name ASC
 * - 장소 추가순: 하위 FavoritePlace.createdAt 의 최댓값 DESC
 *
 * 그룹 삭제 시 하위 FavoritePlace(장소·메모)도 함께 삭제된다.
 */
@Entity
@Table(name = "map_favorite_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteGroup extends BaseEntity {

    /** 그룹명 최대 길이 (공백 포함) */
    public static final int NAME_MAX_LENGTH = 12;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_favorite_group_id")
    private Long id;

    /** 소유 회원 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 그룹명 (1~12자, 공백 포함) */
    @Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    /** 그룹 색상 (팔레트 enum, 기본 BLUE) */
    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false, length = 20)
    private FavoriteGroupColor color;

    /** 그룹 종류 (시스템 기본 / 사용자 생성) */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private FavoriteGroupType type;

    @Builder(access = AccessLevel.PRIVATE)
    private FavoriteGroup(Member member, String name, FavoriteGroupColor color, FavoriteGroupType type) {
        this.member = member;
        this.name = name;
        this.color = color;
        this.type = type;
    }

    /** 사용자 생성 그룹 */
    public static FavoriteGroup ofUser(Member member, String name, FavoriteGroupColor color) {
        return FavoriteGroup.builder()
                .member(member)
                .name(name)
                .color(color != null ? color : FavoriteGroupColor.DEFAULT)
                .type(FavoriteGroupType.USER)
                .build();
    }

    /** 시스템 기본 '내 장소' 그룹 */
    public static FavoriteGroup systemMyPlaces(Member member) {
        return FavoriteGroup.builder()
                .member(member)
                .name("내 장소")
                .color(FavoriteGroupColor.BLUE)
                .type(FavoriteGroupType.SYSTEM_MY_PLACES)
                .build();
    }

    // '버스'는 핀이 아니라 정류장·노선 단위(BusFavorite)라 그룹으로 저장하지 않는다.
    // 그룹 리스트 응답에만 가상 항목으로 노출된다 (FavoriteGroupResponse.virtualBus).

    /** 그룹명/색상 수정 (사용자 그룹 전용). 시스템 그룹 차단은 서비스에서 처리한다. */
    public void update(String name, FavoriteGroupColor color) {
        this.name = name;
        this.color = color != null ? color : FavoriteGroupColor.DEFAULT;
    }

    /** 시스템 기본 제공 그룹인지 여부 */
    public boolean isSystem() {
        return type.isSystem();
    }
}
