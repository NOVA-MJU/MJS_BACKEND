package nova.mjs.domain.thingo.map.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nova.mjs.util.entity.BaseEntity;

/**
 * 그룹에 담긴 장소(핀) 멤버십.
 *
 * 하나의 핀은 여러 그룹에 동시에 담길 수 있고, (그룹, 핀) 조합은 유일하다.
 * 메모는 (그룹, 핀) 단위로 저장되므로, 같은 장소라도 그룹마다 다른 메모를 가질 수 있다.
 * createdAt 은 '장소 추가순' 정렬 기준이 된다.
 */
@Entity
@Table(
        name = "map_favorite_place",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_map_favorite_place_group_pin",
                columnNames = {"group_id", "pin_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoritePlace extends BaseEntity {

    /** 메모 최대 길이 (공백 포함) */
    public static final int MEMO_MAX_LENGTH = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_favorite_place_id")
    private Long id;

    /** 소속 그룹 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private FavoriteGroup group;

    /** 대상 핀(건물/장소) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;

    /** 장소 메모 (최대 30자, 공백 포함). 없으면 null */
    @Column(name = "memo", length = MEMO_MAX_LENGTH)
    private String memo;

    @Builder(access = AccessLevel.PRIVATE)
    private FavoritePlace(FavoriteGroup group, Pin pin, String memo) {
        this.group = group;
        this.pin = pin;
        this.memo = memo;
    }

    public static FavoritePlace of(FavoriteGroup group, Pin pin, String memo) {
        return FavoritePlace.builder()
                .group(group)
                .pin(pin)
                .memo(memo)
                .build();
    }

    /** 메모 변경 */
    public void updateMemo(String memo) {
        this.memo = memo;
    }
}
