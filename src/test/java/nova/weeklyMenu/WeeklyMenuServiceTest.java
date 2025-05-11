package nova.weeklyMenu;

import nova.mjs.MjsApplication;
import nova.mjs.weeklyMenu.DTO.WeeklyMenuResponseDTO;
import nova.mjs.weeklyMenu.exception.WeeklyMenuNotFoundException;
import nova.mjs.weeklyMenu.repository.WeeklyMenuRepository;
import nova.mjs.weeklyMenu.service.WeeklyMenuService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = MjsApplication.class)
class WeeklyMenuServiceIntegrationTest {

    @Autowired
    private WeeklyMenuService weeklyMenuService;

    @Autowired
    private WeeklyMenuRepository weeklyMenuRepository;

    @Test
    @DisplayName("명지대 식단 페이지 실제 크롤링 통합 테스트")
    void testCrawlWeeklyMenuFromRealSite() {
        // when
        List<WeeklyMenuResponseDTO> result = weeklyMenuService.crawlWeeklyMenu();

        // then
        assertThat(result).isNotEmpty();
        result.forEach(dto -> {
            System.out.println("날짜: " + dto.getDate());
            System.out.println("카테고리: " + dto.getMenuCategory());
            System.out.println("메뉴: " + dto.getMeals());
            System.out.println("-----");
        });
    }

    @Test
    @DisplayName("크롤링 실패 시 예외 처리 확인")
    void testCrawlMenuFailureHandling() {
        // given: URL을 잘못 주입했을 때의 상황을 가정해야 하므로 메서드 분리 필요
        try {
            Document doc = Jsoup.connect("https://nonexistent-url.mju.ac.kr").get();
            fail("예외가 발생해야 합니다.");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class);
        }
    }

    @Test
    @Transactional
    @DisplayName("크롤링된 식단이 실제 DB에 저장되는지 확인")
    void testWeeklyMenuPersistedToDB() {
        // when
        weeklyMenuService.crawlWeeklyMenu();

        // then
        var result = weeklyMenuRepository.findAll();
        assertThat(result).isNotEmpty();

        result.forEach(menu -> {
            System.out.println("DB 저장 확인: " + menu.getDate() + " / " + menu.getMenuCategory());
            assertThat(menu.getMeals()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("전체 식단 삭제 메서드가 DB의 모든 식단을 삭제하는지 확인")
    @Transactional
    void testDeleteAllWeeklyMenus() {
        // given
        weeklyMenuService.crawlWeeklyMenu(); // 데이터 생성
        assertThat(weeklyMenuRepository.findAll()).isNotEmpty();

        // when
        weeklyMenuService.deleteAllWeeklyMenus();

        // then
        assertThat(weeklyMenuRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("DB에서 전체 식단 데이터를 정상적으로 가져오는지 확인")
    @Transactional
    void testGetAllWeeklyMenus() {
        // given
        weeklyMenuService.crawlWeeklyMenu(); // 크롤링하여 저장

        // when
        List<WeeklyMenuResponseDTO> menus = weeklyMenuService.getAllWeeklyMenus();

        // then
        assertThat(menus).isNotEmpty();
        menus.forEach(menu -> {
            System.out.println("조회된 식단: " + menu.getDate() + " / " + menu.getMenuCategory());
        });
    }

    @Test
    @DisplayName("저장된 식단이 없을 때 예외를 발생시키는지 확인")
    void testGetAllWeeklyMenusThrowsIfEmpty() {
        // given: DB 비워두기
        weeklyMenuService.deleteAllWeeklyMenus();

        // when & then
        assertThatThrownBy(() -> weeklyMenuService.getAllWeeklyMenus())
                .isInstanceOf(WeeklyMenuNotFoundException.class)
                .hasMessageContaining("저장된 식단 정보가 없습니다.");
    }

}
