package nova.mjs.weeklyMenu.service;

import nova.mjs.weeklyMenu.entity.WeeklyMenu;
import nova.mjs.weeklyMenu.repository.WeeklyMenuRepository;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WeeklyMenuService {

    private final WeeklyMenuRepository menuRepository;

    public WeeklyMenuService(WeeklyMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    // URL 상수 선언
    private static final String url = "https://www.mju.ac.kr/mjukr/8595/subview.do";

    public List<WeeklyMenu> crawlWeeklyMenu() {
        List<WeeklyMenu> weeklyMenus = new ArrayList<>();
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

            for (Element row : rows) {
                Element dateCell = row.selectFirst("th[rowspan]");
                if (dateCell != null) {
                    if (dailyMenu != null) {
                        weeklyMenus.add(dailyMenu);
                        menuRepository.save(dailyMenu);
                    }

                    currentDate = dateCell.text().trim();
                    dailyMenu = new WeeklyMenu();
                    dailyMenu.setDate(currentDate);
                    dailyMenu.setMeals(new HashMap<>());
                }

                Elements cells = row.select("td");
                if (cells.size() > 0 && dailyMenu != null) {
                    String mealType = cells.get(0).text().trim();
                    Element menuCell = row.selectFirst("td.alignL");
                    List<String> menuContent = menuCell != null
                            ? Arrays.asList(menuCell.html().split("<br>"))
                            : Collections.singletonList("등록된 식단 내용이 없습니다.");
                    dailyMenu.getMeals().put(mealType, menuContent);
                }
            }

            if (dailyMenu != null) {
                weeklyMenus.add(dailyMenu);
                menuRepository.save(dailyMenu);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return weeklyMenus;
    }
}