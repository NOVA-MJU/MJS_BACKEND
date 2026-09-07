package nova.mjs.domain.thingo.weeklyMenu.service;

import nova.mjs.domain.thingo.weeklyMenu.entity.WeeklyMenu;
import nova.mjs.domain.thingo.weeklyMenu.entity.enumList.MenuCategory;
import nova.mjs.domain.thingo.weeklyMenu.repository.WeeklyMenuRepository;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 식단 페이지 파싱 단위 테스트.
 *
 * 명지대 식단 페이지는 아직 식단이 올라오지 않은 주에도 날짜 뼈대(월~금 x 조/중/석 = 15행)를
 * 그대로 렌더하고 실제 메뉴 셀(td.alignL)만 통째로 빠진다.
 * 이 뼈대를 정상 데이터로 착각하면 크롤러가 기존 식단을 지우고 빈 껍데기를 저장한다.
 */
class WeeklyMenuServiceTest {

    private final WeeklyMenuService weeklyMenuService =
            new WeeklyMenuService(mock(WeeklyMenuRepository.class), mock(ApplicationEventPublisher.class));

    @Test
    @DisplayName("식단이 게시된 주는 조/중/석 3건을 모두 파싱한다")
    void should_파싱_성공_when_식단이_게시된_주() {
        // given
        String html = wrap("""
                <tr><th rowspan="3">08.31( 월 )</th><td>조식</td><td>등록된 식단내용이(가) 없습니다.</td></tr>
                <tr><td>중식</td><td>-</td><td class="alignL">한식잡채<br>쌀밥<br>미니돈가스&amp;케찹</td><td>-</td></tr>
                <tr><td>석식</td><td>등록된 식단내용이(가) 없습니다.</td></tr>
                """);

        // when
        List<WeeklyMenu> menus = weeklyMenuService.parseWeeklyMenus(Jsoup.parse(html));

        // then
        assertThat(menus).hasSize(3);
        assertThat(menus).allSatisfy(menu -> assertThat(menu.getDate()).isEqualTo("08.31( 월 )"));
        assertThat(menus.get(1).getMenuCategory()).isEqualTo(MenuCategory.LUNCH);
        assertThat(menus.get(1).getMeals()).containsExactly("한식잡채", "쌀밥", "미니돈가스&케찹");
        // 메뉴 셀이 없는 끼니는 안내 문구로 채우되 행 자체는 살린다(운영 페이지와 동일).
        assertThat(menus.get(0).getMeals()).containsExactly("등록된 식단 내용이 없습니다.");
    }

    @Test
    @DisplayName("식단이 아직 게시되지 않은 주는 빈 목록을 반환해 기존 데이터를 지키게 한다")
    void should_빈_목록_반환_when_식단_미게시_주() {
        // given - 날짜 뼈대는 있으나 td.alignL 이 하나도 없는 미게시 주
        String html = wrap("""
                <tr><th rowspan="3">09.07( 월 )</th><td>조식</td><td>등록된 식단내용이(가) 없습니다.</td></tr>
                <tr><td>중식</td><td>등록된 식단내용이(가) 없습니다.</td></tr>
                <tr><td>석식</td><td>등록된 식단내용이(가) 없습니다.</td></tr>
                <tr><th rowspan="3">09.08( 화 )</th><td>조식</td><td>등록된 식단내용이(가) 없습니다.</td></tr>
                <tr><td>중식</td><td>등록된 식단내용이(가) 없습니다.</td></tr>
                <tr><td>석식</td><td>등록된 식단내용이(가) 없습니다.</td></tr>
                """);

        // when
        List<WeeklyMenu> menus = weeklyMenuService.parseWeeklyMenus(Jsoup.parse(html));

        // then
        assertThat(menus).isEmpty();
    }

    private String wrap(String tableRows) {
        return "<div class=\"tableWrap marT50\"><table><tbody>" + tableRows + "</tbody></table></div>";
    }
}
