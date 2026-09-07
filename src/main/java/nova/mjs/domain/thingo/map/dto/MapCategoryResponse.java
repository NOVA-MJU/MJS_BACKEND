package nova.mjs.domain.thingo.map.dto;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.map.entity.Category;
import nova.mjs.domain.thingo.map.entity.CategoryGroup;

import java.util.List;

/**
 * 기본 지도 칩(카테고리) 목록 응답. 그룹 → 칩 → 하위탭 구조로 내려준다.
 *
 * 프론트가 기본 지도 상단/전체 카테고리 바텀시트를 그릴 때 쓸 안정적인 code·label·아이콘·순서를 제공한다.
 * (건물 층별안내도의 칩은 이 API가 아니라 건물 상세의 categoryTabs를 사용한다)
 */
@Getter
@Builder
public class MapCategoryResponse {

    /** 그룹 코드 (예: food, convenience) */
    private final String groupCode;
    /** 그룹 표시 이름 (예: "편의 (Convenience)") */
    private final String groupName;
    /** 그룹 노출 순서 (작을수록 위) */
    private final int groupDisplayOrder;
    /** 이 그룹의 최상위 칩 목록 */
    private final List<Chip> chips;

    public static MapCategoryResponse of(CategoryGroup group, List<Chip> chips) {
        return MapCategoryResponse.builder()
                .groupCode(group.getCode())
                .groupName(group.getName())
                .groupDisplayOrder(group.getDisplayOrder())
                .chips(chips)
                .build();
    }

    /** 최상위 칩 1개 */
    @Getter
    @Builder
    public static class Chip {
        private final String code;
        private final String label;
        private final String iconKey;
        /** 클릭 결과 종류 (PLACE_LIST / BUILDING_LIST / BUS) */
        private final String resultType;
        /** 상단 퀵메뉴 기본 노출 대상인지 */
        private final boolean quickMenu;
        private final int displayOrder;
        /** 하위 탭 (예: 대동명지도 아래 한식/중식). 없으면 빈 목록 */
        private final List<SubTab> subTabs;

        public static Chip of(Category category, List<SubTab> subTabs) {
            return Chip.builder()
                    .code(category.getCode())
                    .label(category.getLabel())
                    .iconKey(category.getIconKey())
                    .resultType(category.getResultType().name())
                    .quickMenu(category.isQuickMenu())
                    .displayOrder(category.getDisplayOrder())
                    .subTabs(subTabs)
                    .build();
        }
    }

    /** 하위 탭 1개 */
    @Getter
    @Builder
    public static class SubTab {
        private final String code;
        private final String label;
        private final String iconKey;
        private final int displayOrder;

        public static SubTab from(Category category) {
            return SubTab.builder()
                    .code(category.getCode())
                    .label(category.getLabel())
                    .iconKey(category.getIconKey())
                    .displayOrder(category.getDisplayOrder())
                    .build();
        }
    }
}
