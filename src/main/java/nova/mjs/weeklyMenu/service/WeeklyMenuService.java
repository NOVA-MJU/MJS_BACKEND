package nova.mjs.weeklyMenu.service;

import nova.mjs.community.DTO.CommunityBoardResponse;
import nova.mjs.weeklyMenu.DTO.WeeklyMenuResponse;
import nova.mjs.weeklyMenu.entity.WeeklyMenu;
import nova.mjs.weeklyMenu.entity.enumList.MenuCategory;
import nova.mjs.weeklyMenu.repository.WeeklyMenuRepository;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WeeklyMenuService {
    //1. 요청값이 뭔지(파라미터, request body) - 없음
    //2. 요청값으로 뭘 할건지 -> 없는데 뭘 합니까
    //3. 응답값이 뭔지 -> 날짜, 카테고리, 메뉴(리스트)
    //4. db에 저장할지 판단 -> 필요하면 엔티티에 있는 메서드로 객체 생성 : 날짜, 카테고리, 메뉴(리스트)
    //5. 레퍼에 접근해서 엔티티 값을 넣어줘

    private final WeeklyMenuRepository menuRepository;

    public WeeklyMenuService(WeeklyMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    // URL 상수 선언
    private static final String url = "https://www.mju.ac.kr/mjukr/8595/subview.do";

    public List<WeeklyMenuResponse> crawlWeeklyMenu() {
        List<WeeklyMenuResponse> responses = new ArrayList<>(); //이렇게 주는 게 맞을까?
        try {
            Document doc = Jsoup.connect(url).get();
            Element tableWrap = doc.selectFirst(".tableWrap.marT50");

            if (tableWrap == null) {
                throw new IllegalArgumentException("테이블을 포함하는 div를 찾을 수 없습니다.");
            }

            Element table = tableWrap.selectFirst("table");
            if (table == null) {
                throw new IllegalArgumentException("테이블을 찾을 수 없습니다.");
            }

            Elements rows = table.select("tbody tr");
            String currentDate = null;
            WeeklyMenu dailyMenu = null;

            List<WeeklyMenu> weeklyMenus = new ArrayList<>();

            for (Element row : rows) {
                Element dateCell = row.selectFirst("th[rowspan]"); //날짜
                if (dateCell != null) {
                    currentDate = dateCell.text().trim(); //날짜 최신화
                }

                Elements cells = row.select("td"); //카테고리가 있는 class
                if (cells.size() > 0 && dailyMenu != null) {
                    String category = cells.get(0).text().trim(); //카테고리 수집
                    Element menuCell = row.selectFirst("td.alignL"); //메뉴가 있는 class
                    List<String> menuContent = menuCell != null
                            ? Arrays.asList(menuCell.html().split("<br>"))
                            : Collections.singletonList("등록된 식단 내용이 없습니다."); //메뉴 수집
                    //dailyMenu.getMeals().put(mealType, menuContent);

                    MenuCategory menuCategory = MenuCategory.valueOf(category);
                    WeeklyMenu weeklyMenu = WeeklyMenu.create(currentDate, menuCategory, menuContent);
                    weeklyMenus.add(weeklyMenu);
                    menuRepository.save(weeklyMenu);
                    //save() : 영속성 컨텍스트의 cache에 먼저 저장 -> 나중에 flush()
                    //vs. saveAndFlush() : 즉시 db에 반영
                    responses.add(WeeklyMenuResponse.fromEntity(weeklyMenu));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return responses;
    }
}