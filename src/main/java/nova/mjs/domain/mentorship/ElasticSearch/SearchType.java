package nova.mjs.domain.mentorship.ElasticSearch;

import java.util.List;

public enum SearchType {

    NOTICE,
    MJU_CALENDAR,
    DEPARTMENT_NOTICE,
    DEPARTMENT_SCHEDULE,
    COMMUNITY,
    NEWS,
    BROADCAST;

    /**
     * 통합 검색 Overview 노출 순서 정의
     *
     * enum 선언 순서와 분리하여
     * UX 정책 변경 시 Service 코드를 건드리지 않도록 한다.
     */
    public static List<SearchType> overviewOrder() {
        return List.of(
                NOTICE,
                MJU_CALENDAR,
                DEPARTMENT_NOTICE,
                DEPARTMENT_SCHEDULE,
                COMMUNITY,
                NEWS,
                BROADCAST
        );
    }

    public static SearchType from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return SearchType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 검색 타입입니다: " + value);
        }
    }
}
