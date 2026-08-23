package nova.mjs.domain.thingo.map.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.map.entity.FavoriteGroup;
import nova.mjs.domain.thingo.map.entity.FavoriteGroupType;
import nova.mjs.domain.thingo.map.repository.FavoriteGroupRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원별 시스템 기본 그룹('내 장소','버스') 보장 담당.
 *
 * 가입 플로우를 건드리지 않기 위해, 즐겨찾기 최초 접근(목록 조회/장소 담기) 시점에
 * 없으면 생성(lazy provisioning)한다. 별도 빈으로 두어 @Transactional 이 정상 적용되게 한다.
 */
@Component
@RequiredArgsConstructor
public class FavoriteGroupProvisioner {

    private final FavoriteGroupRepository groupRepository;

    /** 시스템 기본 그룹이 없으면 생성한다. */
    @Transactional
    public void ensureSystemGroups(Member member) {
        if (!groupRepository.existsByMemberAndType(member, FavoriteGroupType.SYSTEM_MY_PLACES)) {
            groupRepository.save(FavoriteGroup.systemMyPlaces(member));
        }
        if (!groupRepository.existsByMemberAndType(member, FavoriteGroupType.SYSTEM_BUS)) {
            groupRepository.save(FavoriteGroup.systemBus(member));
        }
    }

    /** '내 장소' 그룹을 보장하고 반환한다. (장소 즐겨찾기의 기본 편입 그룹) */
    @Transactional
    public FavoriteGroup ensureMyPlaces(Member member) {
        ensureSystemGroups(member);
        return groupRepository.findByMemberAndType(member, FavoriteGroupType.SYSTEM_MY_PLACES)
                .orElseThrow(() -> new IllegalStateException("시스템 '내 장소' 그룹 보장 실패"));
    }
}
