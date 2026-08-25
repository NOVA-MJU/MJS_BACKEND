package nova.mjs.domain.thingo.map.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.map.dto.*;
import nova.mjs.domain.thingo.map.service.FavoriteGroupService;
import nova.mjs.domain.thingo.map.service.FavoritePlaceService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 명지도 즐겨찾기 그룹 컨트롤러.
 *
 * [제공 API] (모두 로그인 필요)
 * - GET    /api/v1/map/favorites/groups                       그룹 리스트(05-1)
 * - POST   /api/v1/map/favorites/groups                       새 그룹 생성(05-5-3)
 * - PATCH  /api/v1/map/favorites/groups/{groupId}             그룹명/색상 수정(05-5-1-1)
 * - DELETE /api/v1/map/favorites/groups/{groupId}             그룹 삭제(05-5-1-2)
 * - GET    /api/v1/map/favorites/groups/{groupId}/places      그룹 상세 장소 목록(05-1-1)
 * - GET    /api/v1/map/favorites/pins/{pinId}/groups          그룹 선택 바텀시트 조회
 * - PATCH  /api/v1/map/favorites/pins/{pinId}                 그룹 선택 바텀시트 저장(다중 그룹 + 메모)
 *
 * [별 토글] 지도/검색/그룹 상세의 별은 모두 '그룹 선택 바텀시트'(GET pins/{pinId}/groups → PATCH pins/{pinId})로
 * 처리한다. 특정 그룹에서 빼기·완전 해제는 PATCH 의 소속 그룹 집합 replace 로 흡수한다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/map/favorites")
public class MapFavoriteGroupController {

    private final FavoriteGroupService favoriteGroupService;
    private final FavoritePlaceService favoritePlaceService;

    // ============================ 그룹 ============================

    /** 그룹 리스트. sort: latest(기본)/name/place_added. 시스템 그룹 상단 고정. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/groups")
    public ApiResponse<List<FavoriteGroupResponse>> getGroups(
            @RequestParam(value = "sort", defaultValue = FavoriteGroupService.SORT_LATEST) String sort,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(favoriteGroupService.getGroups(userPrincipal.getUsername(), sort));
    }

    /** 새 그룹 생성. */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/groups")
    public ApiResponse<FavoriteGroupResponse> createGroup(
            @RequestBody FavoriteGroupCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(favoriteGroupService.createGroup(userPrincipal.getUsername(), request));
    }

    /** 그룹명/색상 수정 (시스템 그룹 불가). */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/groups/{groupId}")
    public ApiResponse<FavoriteGroupResponse> updateGroup(
            @PathVariable Long groupId,
            @RequestBody FavoriteGroupUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(favoriteGroupService.updateGroup(userPrincipal.getUsername(), groupId, request));
    }

    /** 그룹 삭제 (하위 장소·메모 함께 삭제, 시스템 그룹 불가). */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/groups/{groupId}")
    public ApiResponse<Void> deleteGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        favoriteGroupService.deleteGroup(userPrincipal.getUsername(), groupId);
        return ApiResponse.success();
    }

    // ============================ 그룹 상세(장소) ============================

    /** 그룹 상세: 그룹 헤더 + 장소 목록. sort: place_added(기본)/name. lat/lng 있으면 거리 계산. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/groups/{groupId}/places")
    public ApiResponse<FavoriteGroupDetailResponse> getGroupPlaces(
            @PathVariable Long groupId,
            @RequestParam(value = "sort", defaultValue = FavoritePlaceService.SORT_PLACE_ADDED) String sort,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(
                favoritePlaceService.getGroupPlaces(userPrincipal.getUsername(), groupId, sort, lat, lng));
    }

    // ============================ 그룹 선택 바텀시트 ============================

    /** 그룹 선택 바텀시트 조회: 장소명·기존 메모 + 그룹별 선택여부. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/pins/{pinId}/groups")
    public ApiResponse<PinFavoriteGroupsResponse> getPinGroups(
            @PathVariable Long pinId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(favoritePlaceService.getPinGroups(userPrincipal.getUsername(), pinId));
    }

    /** 그룹 선택 바텀시트 저장: 소속 그룹 집합 replace + 메모 반영. */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/pins/{pinId}")
    public ApiResponse<PinFavoriteGroupsResponse> savePinGroups(
            @PathVariable Long pinId,
            @RequestBody PinFavoriteSaveRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(
                favoritePlaceService.savePinGroups(userPrincipal.getUsername(), pinId, request));
    }
}
