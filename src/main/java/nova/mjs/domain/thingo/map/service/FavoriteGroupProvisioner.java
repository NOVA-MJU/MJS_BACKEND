package nova.mjs.domain.thingo.map.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.map.entity.FavoriteGroup;
import nova.mjs.domain.thingo.map.entity.FavoriteGroupType;
import nova.mjs.domain.thingo.map.repository.FavoriteGroupRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원별 시스템 기본 그룹 보장 담당.
 *
 * DB에 저장되는 시스템 그룹은 '내 장소' 하나뿐이다. ('버스'는 핀이 아니라 정류장·노선 단위라
 * 그룹으로 저장하지 않고, 그룹 리스트 응답에만 가상 항목으로 노출한다.)
 * 가입 플로우를 건드리지 않기 위해, 즐겨찾기 최초 접근(목록 조회/장소 담기) 시점에
 * 없으면 생성(lazy provisioning)한다. 별도 빈으로 두어 @Transactional 이 정상 적용되게 한다.
 */
@Component
@RequiredArgsConstructor
public class FavoriteGroupProvisioner {

    private final FavoriteGroupRepository groupRepository;

    /** 시스템 기본 그룹('내 장소')이 없으면 생성한다. */
    @Transactional
    public void ensureSystemGroups(Member member) {
        ensureMyPlaces(member);
    }

    /** '내 장소' 그룹을 보장하고 반환한다. (장소 즐겨찾기의 기본 편입 그룹) */
    @Transactional
    public FavoriteGroup ensureMyPlaces(Member member) {
        return groupRepository.findByMemberAndType(member, FavoriteGroupType.SYSTEM_MY_PLACES)
                .orElseGet(() -> groupRepository.save(FavoriteGroup.systemMyPlaces(member)));
    }
}
