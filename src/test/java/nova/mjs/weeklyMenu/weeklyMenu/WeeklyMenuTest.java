package nova.mjs.weeklyMenu.weeklyMenu;

import nova.mjs.weeklyMenu.entity.WeeklyMenu;
import nova.mjs.weeklyMenu.entity.enumList.MenuCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyMenuTest {

    @Test
    @DisplayName("WeeklyMenu 생성 테스트")
    void testCreateWeeklyMenu() {
        // given
        String date = "2025-05-11";
        MenuCategory category = MenuCategory.LUNCH;
        var meals = Arrays.asList("된장찌개", "고등어구이", "김치");

        // when
        WeeklyMenu menu = WeeklyMenu.create(date, category, meals);

        // then
        assertThat(menu).isNotNull();
        assertThat(menu.getDate()).isEqualTo(date);
        assertThat(menu.getMenuCategory()).isEqualTo(category);
        assertThat(menu.getMeals()).containsExactly("된장찌개", "고등어구이", "김치");
    }

    @Test
    @DisplayName("기본 생성자 및 빌더 테스트")
    void testBuilderAndDefaults() {
        // when
        WeeklyMenu menu = WeeklyMenu.builder()
                .date("2025-05-11")
                .menuCategory(MenuCategory.BREAKFAST)
                .build();

        // then
        assertThat(menu.getMeals()).isNotNull();
        assertThat(menu.getMeals()).isEmpty(); // 기본값이 빈 리스트인지 확인
    }
}
