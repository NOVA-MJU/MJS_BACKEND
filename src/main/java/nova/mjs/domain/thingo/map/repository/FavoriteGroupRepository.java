package nova.mjs.domain.thingo.map.repository;

import nova.mjs.domain.thingo.map.entity.FavoriteGroup;
import nova.mjs.domain.thingo.map.entity.FavoriteGroupType;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteGroupRepository extends JpaRepository<FavoriteGroup, Long> {

    /** 회원의 전체 그룹 (정렬/상단고정은 서비스에서 처리) */
    List<FavoriteGroup> findByMember(Member member);

    /** 시스템 그룹 조회/보장용 - 회원의 특정 종류 그룹 */
    Optional<FavoriteGroup> findByMemberAndType(Member member, FavoriteGroupType type);

    /** 회원이 해당 종류의 그룹을 이미 가지고 있는지 (시스템 그룹 중복 생성 방지) */
    boolean existsByMemberAndType(Member member, FavoriteGroupType type);
}
