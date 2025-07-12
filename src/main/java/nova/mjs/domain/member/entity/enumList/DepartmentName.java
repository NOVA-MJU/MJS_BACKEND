package nova.mjs.domain.member.entity.enumList;

public enum DepartmentName {

    HUMANITIES_COLLEGE,              // 인문대 <단과대>
    CHINESE_LITERATURE,              // 중어중문학과
    JAPANESE_LITERATURE,             // 일어일문학과
    ARABIC_STUDIES,                  // 아랍지역학과
    KOREAN_STUDIES,                  // 글로벌한국어학
    CREATIVE_WRITING,                // 문예창작학과
    KOREAN_LITERATURE,               // 국어국문학과
    ENGLISH_LITERATURE,              // 영어영문학과
    ART_HISTORY,                     // 미술사, 역사학과
    LIBRARY_SCIENCE,                 // 문헌정보학과
    CULTURAL_CONTENT_STUDIES,        // 글로벌문화콘텐츠학전공
    PHILOSOPHY,                      // 철학과


    SOCIAL_SCIENCES,                 // 사과대 <단과대>
    PUBLIC_ADMINISTRATION,           // 행정학과
    POLITICAL_DIPLOMACY,             // 정치외교학과
    LAW,                             // 법학과
    ECONOMICS,                       // 경제학과
    INTERNATIONAL_TRADE,             // 국제통상학전공 -> 홈피엔 경영대에 있는데 그거 사라지고 사과대로 갔다 함
    APPLIED_STATISTICS,              // 응용통계학과


    BUSINESS,                        // 경영대 <단과대>
    BUSINESS_ADMINISTRATION,         // 경영학과
    GLOBAL_BUSINESS_STUDIES,         // 글로벌비즈니스학과
    MANAGEMENT_INFORMATION_SYSTEMS,  // 경영정보학과


    MEDIA_HUMANLIFE,                 // 미휴라 <단과대>
    DIGITAL_MEDIA_STUDIES,           // 디지털미디어학부
    YOUTH_GUIDANCE_STUDIES,          // 청소년지도학과
    CHILD_STUDIES,                   // 아동학과


    AI_SOFTWARE,                     //인소대 <단과대>
    CONVERGENT_SOFTWARE_STUDIES,     // 융합소프트웨어학부
    DIGITAL_CONTENT_DESIGN_STUDIES,  // 디지털콘텐츠디자인학과


    FUTURE_CONVERGENCE,              //미융대 <단과대>

    HONOR,                           //아너칼리지 <단과대> 자유전공학부

    OTHER;

    /**
     * 대소문자 관계없이 문자열로부터 enum 상수를 변환
     * 예: "english_literature", "ENGLISH_LITERATURE" 모두 허용
     */
    public static DepartmentName fromString(String value) {
        try {
            return DepartmentName.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid department name: " + value);
        }
    }

}
