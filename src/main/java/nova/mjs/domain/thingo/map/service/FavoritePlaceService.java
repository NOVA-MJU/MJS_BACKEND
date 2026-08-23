package nova.mjs.domain.thingo.map.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.map.dto.FavoritePlaceCardResponse;
import nova.mjs.domain.thingo.map.dto.PinFavoriteGroupsResponse;
import nova.mjs.domain.thingo.map.dto.PinFavoriteSaveRequest;
import nova.mjs.domain.thingo.map.dto.PinSummaryResponse;
import nova.mjs.domain.thingo.map.entity.*;
import nova.mjs.domain.thingo.map.exception.FavoriteMemoTooLongException;
import nova.mjs.domain.thingo.map.exception.PinNotFoundException;
import nova.mjs.domain.thingo.map.repository.FavoriteGroupRepository;
import nova.mjs.domain.thingo.map.repository.FavoritePlaceRepository;
import nova.mjs.domain.thingo.map.repository.PinRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 즐겨찾기 장소(그룹 내 멤버십) 서비스.
 *
 * - 그룹 상세(05-1-1)의 장소 카드 목록
 * - '그룹 선택 바텀시트' 조회/저장 (특정 장소를 여러 그룹에 담기 + 메모)
 * - 그룹 상세의 별 토글(그룹에서 제거)
 * - 지도/상세 별 토글 레거시 호환('내 장소' 편입/해제)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoritePlaceService {

    /** 그룹 상세 정렬: 장소 추가순(기본) / 가나다순 */
    public static final String SORT_PLACE_ADDED = "place_added";
    public static final String SORT_NAME = "name";

    private static final Collator KOREAN = Collator.getInstance(Locale.KOREAN);

    private final FavoritePlaceRepository placeRepository;
    private final FavoriteGroupRepository groupRepository;
    private final PinRepository pinRepository;
    private final FavoriteGroupService groupService;
    private final FavoriteGroupProvisioner provisioner;
    private final MapPinService mapPinService;

    /**
     * 그룹 상세(05-1-1)의 장소 카드 목록.
     * ('버스'는 핀 기반이 아니라 그룹으로 저장되지 않으므로 이 엔드포인트로 들어오지 않는다.
     *  버스 즐겨찾기는 버스 도착정보 화면/기존 bus API 로 처리한다.)
     */
    public List<FavoritePlaceCardResponse> getGroupPlaces(String email, Long groupId, String sort,
                                                          Double userLat, Double userLng) {
        FavoriteGroup group = groupService.getOwnedGroup(email, groupId);
        List<FavoritePlace> memberships = placeRepository.findByGroupWithPin(group);
        List<FavoritePlace> sorted = sortMemberships(memberships, sort);

        List<Pin> pins = sorted.stream().map(FavoritePlace::getPin).toList();
        Set<Long> favoriteIds = pins.stream().map(Pin::getId).collect(Collectors.toSet()); // 그룹 내 = 모두 즐겨찾기
        List<PinSummaryResponse> summaries = mapPinService.toSummaries(pins, favoriteIds, userLat, userLng);

        List<FavoritePlaceCardResponse> cards = new ArrayList<>(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            cards.add(FavoritePlaceCardResponse.of(summaries.get(i), sorted.get(i).getMemo()));
        }
        return cards;
    }

    /**
     * '그룹 선택 바텀시트' 조회. 회원 그룹 목록 + 각 그룹의 선택여부/메모(프리필)를 구성한다.
     * '버스' 그룹은 핀을 담을 수 없으므로 제외한다.
     */
    @Transactional
    public PinFavoriteGroupsResponse getPinGroups(String email, Long pinId) {
        Member member = groupService.resolveMember(email);
        provisioner.ensureSystemGroups(member);
        Pin pin = getPin(pinId);

        List<FavoriteGroup> groups = sortForBottomSheet(groupRepository.findByMember(member));

        // 이 핀이 담긴 (그룹id -> 멤버십)
        Map<Long, FavoritePlace> byGroupId = placeRepository.findByMemberAndPin(member, pin).stream()
                .collect(Collectors.toMap(fp -> fp.getGroup().getId(), fp -> fp, (a, b) -> a));

        List<PinFavoriteGroupsResponse.GroupSelection> selections = groups.stream()
                .map(g -> {
                    FavoritePlace fp = byGroupId.get(g.getId());
                    return PinFavoriteGroupsResponse.GroupSelection.builder()
                            .id(g.getId())
                            .name(g.getName())
                            .color(g.getColor().name())
                            .type(g.getType().name())
                            .system(g.isSystem())
                            .placeCount(placeRepository.countByGroup(g))
                            .selected(fp != null)
                            .build();
                })
                .toList();

        String memoPrefill = resolveMemoPrefill(byGroupId, groups);
        return PinFavoriteGroupsResponse.of(pinId, pin.getName(), memoPrefill, selections);
    }

    /**
     * '그룹 선택 바텀시트' 저장. 이 핀의 소속 그룹 집합을 groupIds 로 replace 하고 memo 를 반영한다.
     * groupIds 에 없는 (선택 가능) 그룹에서는 제거하고, 빈 배열이면 전 그룹에서 제거(=즐겨찾기 해제).
     * @return 저장 후 최신 바텀시트 상태
     */
    @Transactional
    public PinFavoriteGroupsResponse savePinGroups(String email, Long pinId, PinFavoriteSaveRequest request) {
        Member member = groupService.resolveMember(email);
        provisioner.ensureSystemGroups(member);
        Pin pin = getPin(pinId);
        String memo = normalizeMemo(request.getMemo());

        Set<Long> targetIds = request.getGroupIds() == null
                ? Set.of() : new HashSet<>(request.getGroupIds());

        // 선택 가능한 그룹 = 회원의 모든 저장 그룹('내 장소' + 사용자 그룹). '버스'는 저장되지 않음.
        List<FavoriteGroup> selectable = groupRepository.findByMember(member);
        Set<Long> selectableIds = selectable.stream().map(FavoriteGroup::getId).collect(Collectors.toSet());

        // 존재하지 않거나 소유하지 않은 그룹 id 는 무시(방어).
        for (FavoriteGroup group : selectable) {
            boolean shouldContain = targetIds.contains(group.getId());
            Optional<FavoritePlace> existing = placeRepository.findByGroupAndPin(group, pin);
            if (shouldContain) {
                if (existing.isPresent()) {
                    existing.get().updateMemo(memo);
                } else {
                    placeRepository.save(FavoritePlace.of(group, pin, memo));
                }
            } else {
                existing.ifPresent(placeRepository::delete);
            }
        }
        log.debug("즐겨찾기 그룹 저장 - email={}, pinId={}, groups={}", email,
                pinId, targetIds.stream().filter(selectableIds::contains).toList());

        return getPinGroups(email, pinId);
    }

    /**
     * 그룹 상세의 별 토글: 해당 그룹에서 이 핀 제거(멱등).
     * UI 는 페이지 이탈 전까지 카드를 유지하므로 응답은 성공만 내려준다.
     */
    @Transactional
    public void removePinFromGroup(String email, Long groupId, Long pinId) {
        FavoriteGroup group = groupService.getOwnedGroup(email, groupId);
        Pin pin = getPin(pinId);
        placeRepository.findByGroupAndPin(group, pin).ifPresent(placeRepository::delete);
        log.debug("그룹에서 장소 제거 - email={}, groupId={}, pinId={}", email, groupId, pinId);
    }

    /**
     * 레거시 별 토글 호환: '내 장소' 그룹에 편입/해제.
     * @return true: 추가됨, false: 해제됨
     */
    @Transactional
    public boolean toggleMyPlaces(String email, Long pinId) {
        Member member = groupService.resolveMember(email);
        FavoriteGroup myPlaces = provisioner.ensureMyPlaces(member);
        Pin pin = getPin(pinId);

        Optional<FavoritePlace> existing = placeRepository.findByGroupAndPin(myPlaces, pin);
        if (existing.isPresent()) {
            placeRepository.delete(existing.get());
            return false;
        }
        placeRepository.save(FavoritePlace.of(myPlaces, pin, null));
        return true;
    }

    // ====================== 내부 헬퍼 ======================

    private Pin getPin(Long pinId) {
        return pinRepository.findById(pinId).orElseThrow(PinNotFoundException::new);
    }

    private String normalizeMemo(String memo) {
        if (memo == null) return null;
        if (memo.length() > FavoritePlace.MEMO_MAX_LENGTH) {
            throw new FavoriteMemoTooLongException();
        }
        return memo.isBlank() ? null : memo;
    }

    /** 그룹 상세 정렬: 장소 추가순(createdAt DESC, 기본) 또는 가나다순(핀 이름 ASC) */
    private List<FavoritePlace> sortMemberships(List<FavoritePlace> memberships, String sort) {
        Comparator<FavoritePlace> comparator;
        if (SORT_NAME.equals(sort)) {
            comparator = Comparator.comparing(fp -> fp.getPin().getName(), KOREAN);
        } else {
            comparator = Comparator.comparing(FavoritePlace::getCreatedAt,
                    Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder()));
        }
        return memberships.stream().sorted(comparator).toList();
    }

    /** 바텀시트용 그룹 정렬: '내 장소' 상단 고정 후 최신순 (바텀시트에 '버스'는 노출되지 않음) */
    private List<FavoriteGroup> sortForBottomSheet(List<FavoriteGroup> groups) {
        Comparator<FavoriteGroup> systemFirst = Comparator.comparingInt(
                g -> g.getType() == FavoriteGroupType.SYSTEM_MY_PLACES ? 0 : 1);
        Comparator<FavoriteGroup> latest = Comparator.comparing(FavoriteGroup::getCreatedAt,
                Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder()));
        return groups.stream().sorted(systemFirst.thenComparing(latest)).toList();
    }

    /** 메모 프리필: '내 장소' 멤버십 메모 우선, 없으면 최초 멤버십 메모 */
    private String resolveMemoPrefill(Map<Long, FavoritePlace> byGroupId, List<FavoriteGroup> groups) {
        for (FavoriteGroup g : groups) {
            if (g.getType() == FavoriteGroupType.SYSTEM_MY_PLACES) {
                FavoritePlace fp = byGroupId.get(g.getId());
                if (fp != null && fp.getMemo() != null) return fp.getMemo();
            }
        }
        return byGroupId.values().stream()
                .map(FavoritePlace::getMemo)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
