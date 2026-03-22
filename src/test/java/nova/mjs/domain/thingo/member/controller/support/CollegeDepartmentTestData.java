package nova.mjs.domain.thingo.member.controller.support;

import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class CollegeDepartmentTestData {

    private static final Map<College, Set<DepartmentName>> VALID_COMBINATIONS = createCombinations();

    private CollegeDepartmentTestData() {
    }

    public static Stream<CollegeDepartmentPair> validPairs() {
        return VALID_COMBINATIONS.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(department -> new CollegeDepartmentPair(entry.getKey(), department)));
    }

    private static Map<College, Set<DepartmentName>> createCombinations() {
        Map<College, Set<DepartmentName>> combinations = new EnumMap<>(College.class);

        // 인문대학
        combinations.put(College.HUMANITIES, EnumSet.of(
                DepartmentName.ASIA_MIDDLE_EAST_LANGUAGES,
                DepartmentName.CHINESE_LITERATURE,
                DepartmentName.JAPANESE_LITERATURE,
                DepartmentName.ARABIC_STUDIES,
                DepartmentName.KOREAN_STUDIES,
                DepartmentName.CULTURAL_CONTENT_STUDIES,
                DepartmentName.KOREAN_LITERATURE,
                DepartmentName.ENGLISH_LITERATURE,
                DepartmentName.ART_HISTORY,
                DepartmentName.ART_HISTORY_DEPARTMENT,
                DepartmentName.HISTORY_DEPARTMENT,
                DepartmentName.LIBRARY_SCIENCE,
                DepartmentName.GLOBAL_CULTURAL_CONTENT_STUDIES,
                DepartmentName.CREATIVE_WRITING,
                DepartmentName.PHILOSOPHY
        ));

        // 사회과학대학
        combinations.put(College.SOCIAL_SCIENCES, EnumSet.of(
                DepartmentName.PUBLIC_ADMINISTRATION,
                DepartmentName.SCHOOL_OF_PUBLIC_SERVICE,
                DepartmentName.POLITICAL_DIPLOMACY,
                DepartmentName.ECONOMICS_STATISTICS,
                DepartmentName.ECONOMICS,
                DepartmentName.INTERNATIONAL_TRADE,
                DepartmentName.APPLIED_STATISTICS,
                DepartmentName.LAW
        ));

        // 경영대학
        combinations.put(College.BUSINESS, EnumSet.of(
                DepartmentName.BUSINESS_DEPARTMENT,
                DepartmentName.BUSINESS_ADMINISTRATION,
                DepartmentName.GLOBAL_BUSINESS_STUDIES,
                DepartmentName.MANAGEMENT_INFORMATION_SYSTEMS
        ));

        // 미디어·휴먼라이프대학
        combinations.put(College.MEDIA_HUMANLIFE, EnumSet.of(
                DepartmentName.DIGITAL_MEDIA_STUDIES,
                DepartmentName.YOUTH_GUIDANCE_CHILD_STUDIES,
                DepartmentName.YOUTH_GUIDANCE_STUDIES,
                DepartmentName.CHILD_STUDIES
        ));

        // 인공지능·소프트웨어융합대학
        combinations.put(College.AI_SOFTWARE, EnumSet.of(
                DepartmentName.CONVERGENT_SOFTWARE_STUDIES,
                DepartmentName.APPLICATION_SOFTWARE,
                DepartmentName.DATA_SCIENCE,
                DepartmentName.AI,
                DepartmentName.DIGITAL_CONTENT_DESIGN_STUDIES
        ));

        // 미래융합대학
        combinations.put(College.FUTURE_CONVERGENCE, EnumSet.of(
                DepartmentName.CREATIVE_CONVERGENCE_TALENT_DEPARTMENT,
                DepartmentName.SOCIAL_WELFARE,
                DepartmentName.REAL_ESTATE,
                DepartmentName.LAW_ADMINISTRATION,
                DepartmentName.PSYCHOLOGY_THERAPY,
                DepartmentName.FUTURE_CONVERGENCE_BUSINESS,
                DepartmentName.MULTI_DESIGN,
                DepartmentName.ACCOUNTING_TAXATION,
                DepartmentName.CONTRACT
        ));

        // 아너칼리지
        combinations.put(College.HONOR, EnumSet.of(DepartmentName.FREE_MAJOR));

        return combinations;
    }

    public record CollegeDepartmentPair(College college, DepartmentName departmentName) {
    }
}
