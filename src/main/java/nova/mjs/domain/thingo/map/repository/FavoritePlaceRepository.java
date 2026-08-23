package nova.mjs.domain.thingo.map.repository;

import nova.mjs.domain.thingo.map.entity.FavoriteGroup;
import nova.mjs.domain.thingo.map.entity.FavoritePlace;
import nova.mjs.domain.thingo.map.entity.Pin;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {

    /** 그룹 내 장소 수 */
    long countByGroup(FavoriteGroup group);

    /** 그룹 상세 - 그룹 내 장소 목록 (핀·카테고리 fetch join, 장소 추가순 기본). 정렬은 서비스에서 재적용 가능 */
    @Query("select fp from FavoritePlace fp " +
            "join fetch fp.pin p " +
            "join fetch p.category " +
            "where fp.group = :group")
    List<FavoritePlace> findByGroupWithPin(@Param("group") FavoriteGroup group);

    /** 토글/중복 확인 - 그룹에 해당 핀이 담겨 있는지 */
    Optional<FavoritePlace> findByGroupAndPin(FavoriteGroup group, Pin pin);

    boolean existsByGroupAndPin(FavoriteGroup group, Pin pin);

    /** 바텀시트 - 특정 회원이 이 핀을 담아둔 모든 멤버십(그룹별 선택여부·메모 구성용) */
    @Query("select fp from FavoritePlace fp " +
            "where fp.pin = :pin and fp.group.member = :member")
    List<FavoritePlace> findByMemberAndPin(@Param("member") Member member, @Param("pin") Pin pin);

    /** 지도/검색 별표시용 - 회원이 어떤 그룹에든 담아둔 핀 ID 집합 */
    @Query("select distinct fp.pin.id from FavoritePlace fp where fp.group.member = :member")
    List<Long> findDistinctPinIdsByMember(@Param("member") Member member);

    /** 마이페이지 집계용 - 회원이 즐겨찾기한 서로 다른 핀 수 */
    @Query("select count(distinct fp.pin.id) from FavoritePlace fp where fp.group.member = :member")
    long countDistinctPinsByMember(@Param("member") Member member);

    /** 그룹 리스트 - 그룹별 장소 수 [groupId, count] */
    @Query("select fp.group.id, count(fp) from FavoritePlace fp where fp.group in :groups group by fp.group.id")
    List<Object[]> countByGroups(@Param("groups") List<FavoriteGroup> groups);

    /** 그룹 리스트('장소 추가순') - 그룹별 마지막 장소 추가 시각 [groupId, maxCreatedAt] */
    @Query("select fp.group.id, max(fp.createdAt) from FavoritePlace fp where fp.group in :groups group by fp.group.id")
    List<Object[]> lastAddedAtByGroups(@Param("groups") List<FavoriteGroup> groups);

    /** 그룹 삭제 시 하위 멤버십 일괄 삭제 */
    void deleteByGroup(FavoriteGroup group);
}
