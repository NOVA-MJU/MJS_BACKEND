package nova.mjs.domain.thingo.map.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 명지도 검색 전용 응답.
 *
 * resultType이 data 배열 전체의 스키마와 화면 이동 방식을 결정한다.
 * 공통 ApiResponse에 필드를 추가하지 않아 다른 API의 응답 계약은 변경하지 않는다.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MapSearchResponse {

    private final String status;
    private final MapSearchResultType resultType;
    private final List<?> data;
    private final LocalDateTime timestamp;

    public static MapSearchResponse success(MapSearchResultType resultType, List<?> data) {
        return new MapSearchResponse("API 요청 성공", resultType, data, LocalDateTime.now());
    }
}
