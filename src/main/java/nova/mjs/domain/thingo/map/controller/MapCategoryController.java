package nova.mjs.domain.thingo.map.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.map.dto.MapCategoryResponse;
import nova.mjs.domain.thingo.map.dto.PinSummaryResponse;
import nova.mjs.domain.thingo.map.dto.MapMarkerResponse;
import nova.mjs.domain.thingo.map.service.MapPinService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 명지도 카테고리(칩) 컨트롤러.
 *
 * [제공 API]
 * 1. GET /api/v1/map/categories/{code}/pins  - 특정 칩 클릭 시 장소/건물 목록
 *
 * 전체 카테고리 목록/상단 퀵메뉴 구성은 프론트가 자체 보유(onClick 매핑)하므로 백엔드는 제공하지 않는다.
 *
 * [인증]
 * - 모두 비로그인 가능. 로그인 시 즐겨찾기 마킹/상단 정렬이 적용된다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/map")
public class MapCategoryController {

    private final MapPinService mapPinService;

    /**
     * 기본 지도 칩(카테고리) 목록 조회.
     *
     * 그룹 → 최상위 칩 → 하위 탭 구조로, 노출 순서대로 내려준다.
     * 프론트는 이 응답으로 기본 지도 칩셋을 렌더한다(코드 하드코딩 불필요).
     * 층별안내도 칩셋은 이 API가 아니라 건물 상세(/buildings/{id})의 categoryTabs를 사용한다.
     */
    @GetMapping("/categories")
    public ApiResponse<List<MapCategoryResponse>> getCategories() {
        return ApiResponse.success(mapPinService.getCategories());
    }

    /**
     * 특정 칩을 눌렀을 때 보여줄 장소/건물 목록.
     *
     * - 정렬: 즐겨찾기 먼저 → 현재 위치에서 가까운 순 (캠퍼스 밖이면 정문 기준 정렬)
     * - 장소 목록은 무한 스크롤용으로 page/size로 잘라 반환한다 (건물 칩은 수가 적음)
     * - 버스 칩(result_type=BUS)은 빈 목록을 반환한다. 프론트는 result_type을 보고 버스 화면으로 이동한다.
     *
     * @param code 칩 코드 (예: "daedong", "printer")
     * @param lat  사용자 현재 위도 (없으면 거리 미계산)
     * @param lng  사용자 현재 경도
     * @param page 0부터 시작하는 페이지 번호
     * @param size 페이지 크기
     * @param seed 대동명지도 추천 순서를 세션 동안 고정할 선택 seed
     * @param floorMap 건물 안 시설을 층별안내도(FLOOR_MAP)로 라우팅할지 여부.
     *                 기본 false면 내부 시설도 type=PLACE·link=null로 내려간다(하위호환).
     *                 true일 때만 type=FLOOR_MAP·층별안내도 link를 제공한다.
     */
    @GetMapping("/categories/{code}/pins")
    public ApiResponse<List<PinSummaryResponse>> getPinsByCategory(
            @PathVariable("code") String code,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "seed", required = false) String seed,
            @RequestParam(value = "floorMap", defaultValue = "false") boolean floorMap,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String email = (userPrincipal != null) ? userPrincipal.getUsername() : null;
        log.info("[명지도 칩 목록] code={}, lat={}, lng={}, page={}, floorMap={}, email={}",
                code, lat, lng, page, floorMap, email);
        List<PinSummaryResponse> pins = mapPinService.getPinsByCategory(code, lat, lng, page, size, email, seed, floorMap);
        return ApiResponse.success(applyFloorMap(pins, floorMap));
    }

    /**
     * floorMap=true면 서비스가 내부 시설만 FLOOR_MAP으로 내려주므로 그대로 반환한다.
     * floorMap=false(기본)면 FLOOR_MAP 항목을 기본 PLACE로 낮춰 하위호환을 유지한다.
     */
    private List<PinSummaryResponse> applyFloorMap(List<PinSummaryResponse> pins, boolean floorMap) {
        return floorMap ? pins : pins.stream().map(PinSummaryResponse::toPlaceFallback).toList();
    }

    /** 지도 마커 전용 조회. 내부 시설은 건물별 대표 마커 하나로 묶는다. */
    @GetMapping("/categories/{code}/markers")
    public ApiResponse<List<MapMarkerResponse>> getMarkersByCategory(
            @PathVariable("code") String code
    ) {
        return ApiResponse.success(mapPinService.getMarkersByCategory(code));
    }
}
