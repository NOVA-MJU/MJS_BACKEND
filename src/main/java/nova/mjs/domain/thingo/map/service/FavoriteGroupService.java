package nova.mjs.domain.thingo.map.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.map.dto.FavoriteGroupCreateRequest;
import nova.mjs.domain.thingo.map.dto.FavoriteGroupResponse;
import nova.mjs.domain.thingo.map.dto.FavoriteGroupUpdateRequest;
import nova.mjs.domain.thingo.map.entity.FavoriteGroup;
import nova.mjs.domain.thingo.map.entity.FavoriteGroupColor;
import nova.mjs.domain.thingo.map.entity.FavoriteGroupType;
import nova.mjs.domain.thingo.map.exception.FavoriteGroupForbiddenException;
import nova.mjs.domain.thingo.map.exception.FavoriteGroupNameInvalidException;
import nova.mjs.domain.thingo.map.exception.FavoriteGroupNotFoundException;
import nova.mjs.domain.thingo.map.exception.SystemFavoriteGroupModificationException;
import nova.mjs.domain.thingo.map.repository.BusFavoriteRepository;
import nova.mjs.domain.thingo.map.repository.FavoriteGroupRepository;
import nova.mjs.domain.thingo.map.repository.FavoritePlaceRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.exception.MemberNotFoundException;
import nova.mjs.domain.thingo.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 즐겨찾기 그룹 서비스.
 *
 * 그룹 목록/생성/수정/삭제와 시스템 그룹 보장을 담당한다.
 * - 시스템 그룹('내 장소','버스')은 정렬과 무관하게 항상 상단 고정('내 장소' → '버스' 순).
 * - 시스템 그룹은 수정/삭제 불가.
 * - '버스' 그룹의 저장 개수는 BusFavorite 에서, 그 외 그룹은 FavoritePlace 에서 집계한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteGroupService {

    /** 정렬 기준 */
    public static final String SORT_LATEST = "latest";       // 그룹 생성일 최신순 (기본)
    public static final String SORT_NAME = "name";           // 가나다순
    public static final String SORT_PLACE_ADDED = "place_added"; // 장소 추가순 (그룹 내 최근 추가일)

    private static final Collator KOREAN = Collator.getInstance(Locale.KOREAN);

    private final FavoriteGroupRepository groupRepository;
    private final FavoritePlaceRepository placeRepository;
    private final BusFavoriteRepository busFavoriteRepository;
    private final MemberRepository memberRepository;
    private final FavoriteGroupProvisioner provisioner;

    /**
     * 그룹 리스트(05-1). 시스템 그룹 상단 고정 후 사용자 그룹을 sort 기준으로 정렬한다.
     */
    @Transactional
    public List<FavoriteGroupResponse> getGroups(String email, String sort) {
        Member member = resolveMember(email);
        provisioner.ensureSystemGroups(member);

        List<FavoriteGroup> groups = groupRepository.findByMember(member);

        Map<Long, Long> placeCounts = placeCountsByGroup(groups);
        long busCount = busFavoriteRepository.countByMember(member);
        Map<Long, LocalDateTime> lastAdded = lastAddedByGroup(groups);

        Comparator<FavoriteGroup> systemFirst = Comparator.comparingInt(this::systemRank);
        Comparator<FavoriteGroup> userOrder = userComparator(sort, lastAdded);

        return groups.stream()
                .sorted(systemFirst.thenComparing(userOrder))
                .map(g -> FavoriteGroupResponse.of(g, placeCount(g, placeCounts, busCount)))
                .toList();
    }

    /** 새 그룹 생성(05-5-3). */
    @Transactional
    public FavoriteGroupResponse createGroup(String email, FavoriteGroupCreateRequest request) {
        Member member = resolveMember(email);
        provisioner.ensureSystemGroups(member);

        String name = validateName(request.getName());
        FavoriteGroupColor color = parseColor(request.getColor());

        FavoriteGroup saved = groupRepository.save(FavoriteGroup.ofUser(member, name, color));
        log.debug("즐겨찾기 그룹 생성 - email={}, name={}, color={}", email, name, color);
        return FavoriteGroupResponse.of(saved, 0L);
    }

    /** 그룹명/색상 수정(05-5-1-1). 시스템 그룹 불가. */
    @Transactional
    public FavoriteGroupResponse updateGroup(String email, Long groupId, FavoriteGroupUpdateRequest request) {
        FavoriteGroup group = getOwnedGroup(email, groupId);
        if (group.isSystem()) {
            throw new SystemFavoriteGroupModificationException();
        }
        String name = validateName(request.getName());
        FavoriteGroupColor color = parseColor(request.getColor());

        group.update(name, color);
        long count = placeRepository.countByGroup(group);
        log.debug("즐겨찾기 그룹 수정 - email={}, groupId={}, name={}", email, groupId, name);
        return FavoriteGroupResponse.of(group, count);
    }

    /** 그룹 삭제(05-5-1-2). 하위 장소·메모도 함께 삭제. 시스템 그룹 불가. */
    @Transactional
    public void deleteGroup(String email, Long groupId) {
        FavoriteGroup group = getOwnedGroup(email, groupId);
        if (group.isSystem()) {
            throw new SystemFavoriteGroupModificationException();
        }
        placeRepository.deleteByGroup(group);
        groupRepository.delete(group);
        log.debug("즐겨찾기 그룹 삭제 - email={}, groupId={}", email, groupId);
    }

    // ====================== 내부 공용 (다른 서비스에서 재사용) ======================

    /** 이메일로 회원 조회 (없으면 예외) */
    public Member resolveMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }

    /** 그룹 소유권 확인 후 반환 (없으면 404, 남의 것이면 403) */
    public FavoriteGroup getOwnedGroup(String email, Long groupId) {
        Member member = resolveMember(email);
        FavoriteGroup group = groupRepository.findById(groupId)
                .orElseThrow(FavoriteGroupNotFoundException::new);
        if (!group.getMember().getId().equals(member.getId())) {
            throw new FavoriteGroupForbiddenException();
        }
        return group;
    }

    /** 그룹명 검증 (1~12자, 공백 포함). 유효하면 그대로 반환. */
    public String validateName(String name) {
        if (name == null || name.isEmpty() || name.length() > FavoriteGroup.NAME_MAX_LENGTH) {
            throw new FavoriteGroupNameInvalidException();
        }
        return name;
    }

    /** 색상 파싱 (미지정/공백 → 기본 BLUE, 잘못된 값 → 기본 BLUE). */
    public FavoriteGroupColor parseColor(String color) {
        if (color == null || color.isBlank()) {
            return FavoriteGroupColor.DEFAULT;
        }
        try {
            return FavoriteGroupColor.valueOf(color.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return FavoriteGroupColor.DEFAULT;
        }
    }

    // ====================== 내부 헬퍼 ======================

    /** 시스템 그룹 상단 고정 순위 ('내 장소'=0, '버스'=1, 사용자=2) */
    private int systemRank(FavoriteGroup g) {
        if (g.getType() == FavoriteGroupType.SYSTEM_MY_PLACES) return 0;
        if (g.getType() == FavoriteGroupType.SYSTEM_BUS) return 1;
        return 2;
    }

    /** 사용자 그룹 정렬 비교자 (시스템 그룹끼리는 systemRank 로 이미 정렬됨) */
    private Comparator<FavoriteGroup> userComparator(String sort, Map<Long, LocalDateTime> lastAdded) {
        if (SORT_NAME.equals(sort)) {
            return Comparator.comparing(FavoriteGroup::getName, KOREAN);
        }
        if (SORT_PLACE_ADDED.equals(sort)) {
            // 최근 장소 추가일 DESC, 장소 없는 그룹은 뒤로
            return Comparator.comparing(
                    (FavoriteGroup g) -> lastAdded.get(g.getId()),
                    Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder()));
        }
        // 기본: 최신순 (생성일 DESC)
        return Comparator.comparing(FavoriteGroup::getCreatedAt,
                Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder()));
    }

    private long placeCount(FavoriteGroup g, Map<Long, Long> placeCounts, long busCount) {
        if (g.getType() == FavoriteGroupType.SYSTEM_BUS) {
            return busCount;
        }
        return placeCounts.getOrDefault(g.getId(), 0L);
    }

    private Map<Long, Long> placeCountsByGroup(List<FavoriteGroup> groups) {
        Map<Long, Long> map = new HashMap<>();
        if (groups.isEmpty()) return map;
        for (Object[] row : placeRepository.countByGroups(groups)) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }

    private Map<Long, LocalDateTime> lastAddedByGroup(List<FavoriteGroup> groups) {
        Map<Long, LocalDateTime> map = new HashMap<>();
        if (groups.isEmpty()) return map;
        for (Object[] row : placeRepository.lastAddedAtByGroups(groups)) {
            map.put((Long) row[0], (LocalDateTime) row[1]);
        }
        return map;
    }
}
