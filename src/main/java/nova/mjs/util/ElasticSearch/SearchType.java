package nova.mjs.util.ElasticSearch;

public enum SearchType {
    NOTICE,
    DEPARTMENT_NOTICE,
    DEPARTMENT_SCHEDULE,
    COMMUNITY,
    NEWS,
    BROADCAST,
    MJU_CALENDAR;
//    ALL;

    public static SearchType from(String value) {
//        if (value == null || value.isBlank()) {
//            return ALL;
//        }

        try {
            return SearchType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 검색 타입입니다: " + value);
        }
    }
}
