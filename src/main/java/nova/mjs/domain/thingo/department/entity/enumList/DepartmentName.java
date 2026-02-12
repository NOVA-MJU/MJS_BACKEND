package nova.mjs.domain.thingo.department.entity.enumList;

import lombok.Getter;

@Getter
public enum DepartmentName {

    /* =========================================================
     * 인문대학
     * ========================================================= */
    ASIA_MIDDLE_EAST_LANGUAGES("아시아·중동어문학부"),
    CHINESE_LITERATURE("중어중문학전공"),
    JAPANESE_LITERATURE("일어일문학전공"),
    ARABIC_STUDIES("아랍지역학전공"),
    KOREAN_STUDIES("글로벌한국어학전공"),
    CULTURAL_CONTENT_STUDIES("인문콘텐츠학부"),
    KOREAN_LITERATURE("국어국문학전공"),
    ENGLISH_LITERATURE("영어영문학전공"),
    ART_HISTORY_DEPARTMENT("미술사학과"),
    HISTORY_DEPARTMENT("사학과"),
    ART_HISTORY("미술사·역사학전공"),
    LIBRARY_SCIENCE("문헌정보학전공"),
    GLOBAL_CULTURAL_CONTENT_STUDIES("글로벌문화콘텐츠학전공"),
    CREATIVE_WRITING("문예창작학과"),
    PHILOSOPHY("철학과"),

    /* =========================================================
     * 사회과학대학
     * ========================================================= */
    PUBLIC_ADMINISTRATION("행정학전공"),
    POLITICAL_DIPLOMACY("정치외교학전공"),
    ECONOMICS_STATISTICS("경상·통계학부"),
    ECONOMICS("경제학전공"),
    INTERNATIONAL_TRADE("국제통상학전공"),
    APPLIED_STATISTICS("응용통계학전공"),
    LAW("법학과"),

    /* =========================================================
     * 경영대학
     * ========================================================= */
    BUSINESS_DEPARTMENT("경영학부"),
    BUSINESS_ADMINISTRATION("경영학전공"),
    GLOBAL_BUSINESS_STUDIES("글로벌비즈니스학전공"),
    MANAGEMENT_INFORMATION_SYSTEMS("경영정보학과"),

    /* =========================================================
     * 미디어·휴먼라이프대학
     * ========================================================= */
    DIGITAL_MEDIA_STUDIES("디지털미디어학부"),
    YOUTH_GUIDANCE_CHILD_STUDIES("청소년지도·아동학부"),
    YOUTH_GUIDANCE_STUDIES("청소년지도학전공"),
    CHILD_STUDIES("아동학전공"),

    /* =========================================================
     * 인공지능·소프트웨어융합대학
     * ========================================================= */
    CONVERGENT_SOFTWARE_STUDIES("융합소프트웨어학부"),
    APPLICATION_SOFTWARE("응용소프트웨어전공"),
    DATA_SCIENCE("데이터사이언스전공"),
    AI("인공지능전공"),
    DIGITAL_CONTENT_DESIGN_STUDIES("디지털콘텐츠디자인학과"),

    /* =========================================================
     * 미래융합대학
     * ========================================================= */
    CREATIVE_CONVERGENCE_TALENT_DEPARTMENT("창의융합인재학부"),
    SOCIAL_WELFARE("사회복지학과"),
    REAL_ESTATE("부동산학과"),
    LAW_ADMINISTRATION("법무행정학과"),
    PSYCHOLOGY_THERAPY("심리치료학과"),
    FUTURE_CONVERGENCE_BUSINESS("미래융합경영학과"),
    MULTI_DESIGN("멀티디자인학과"),
    ACCOUNTING_TAXATION("회계세무학과"),
    CONTRACT("계약학과"),

    /* =========================================================
     * 아너칼리지
     * ========================================================= */
    FREE_MAJOR("자율전공학부(인문)"),

    /* =========================================================
     * 기타
     * ========================================================= */
    OTHER("기타");

    private final String label;

    DepartmentName(String label) {
        this.label = label;
    }

    /**
     * 대소문자 관계없이 문자열로부터 enum 상수를 변환
     * 프론트 value 그대로 받아 매핑하기 위한 메서드
     */
    public static DepartmentName fromString(String value) {
        try {
            return DepartmentName.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid department name: " + value);
        }
    }
}
