package nova.mjs.domain.thingo.map.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.map.dto.MapSuggestResponse;
import nova.mjs.domain.thingo.map.dto.MapSearchResponse;
import nova.mjs.domain.thingo.map.service.MapSearchService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 명지도 특화 검색 컨트롤러.
 *
 * 기존 통합검색(/api/v1/search)과 분리된 명지도(건물/장소) 전용 검색이다.
 *
 * [제공 API]
 * 1. GET /api/v1/map/search           - 검색 결과 목록 (무한 스크롤)
 * 2. GET /api/v1/map/search/suggest   - 검색 자동완성 (경량)
 *
 * [공통 파라미터]
 * - keyword: 검색어 (실내 title 정확일치 → 라벨 정확일치 → 일반 이름 검색 순으로 판정)
 * - lat/lng: 프론트 GPS 좌표. 거리 계산/정렬에만 쓰고 저장하지 않는다
 *
 * [인증]
 * - 비로그인 가능. 로그인 시 즐겨찾기 여부가 표시된다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/map")
public class MapSearchController {

    private final MapSearchService mapSearchService;

    /**
     * 명지도 검색 결과 조회.
     * 응답의 resultType은 data 배열 전체에 적용되며 FLOOR_MAP/LABEL/GENERAL 중 하나다.
     */
    @GetMapping("/search")
    public MapSearchResponse search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "seed", required = false) String seed,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String email = (userPrincipal != null) ? userPrincipal.getUsername() : null;
        log.info("[명지도 검색] keyword={}, lat={}, lng={}, page={}, email={}",
                keyword, lat, lng, page, email);
        return mapSearchService.search(keyword, lat, lng, page, size, email, seed);
    }

    /**
     * 명지도 검색 자동완성 조회.
     */
    @GetMapping("/search/suggest")
    public ApiResponse<List<MapSuggestResponse>> suggest(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        log.info("[명지도 자동완성] keyword={}, limit={}", keyword, limit);
        return ApiResponse.success(mapSearchService.suggest(keyword, limit));
    }
}
