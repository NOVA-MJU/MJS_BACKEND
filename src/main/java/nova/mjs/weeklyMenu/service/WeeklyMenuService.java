package nova.mjs.weeklyMenu.service;

import lombok.extern.log4j.Log4j2;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.weeklyMenu.DTO.WeeklyMenuResponseDTO;
import nova.mjs.weeklyMenu.entity.WeeklyMenu;
import nova.mjs.weeklyMenu.entity.enumList.MenuCategory;
import nova.mjs.weeklyMenu.exception.WeeklyMenuNotFoundException;
import nova.mjs.weeklyMenu.repository.WeeklyMenuRepository;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Log4j2
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

    @Transactional
    public List<WeeklyMenuResponseDTO> crawlWeeklyMenu() {
        List<WeeklyMenu> weeklyMenus = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url).get();
            Element tableWrap = doc.selectFirst(".tableWrap.marT50");

            if (tableWrap == null) {
                log.error("테이블을 포함하는 div를 찾을 수 없습니다.");
            }

            Element table = tableWrap.selectFirst("table");
            if (table == null) {
                log.error("테이블을 찾을 수 없습니다.");
            }

            Elements rows = table.select("tbody tr");
            if (rows.isEmpty()){
                log.error("식단 데이터를 찾을 수 없습니다.");
                throw new WeeklyMenuNotFoundException("식단 데이터를 찾을 수 없습니다.", ErrorCode.WEEKLYMENU_NOT_FOUND);
            }
            String currentDate = null;

            for (Element row : rows) {
                Element dateCell = row.selectFirst("th[rowspan]"); //날짜
                if (dateCell != null) {
                    currentDate = dateCell.text().trim(); //날짜 최신화
                }

                Elements cells = row.select("td"); //카테고리가 있는 class
                if (!cells.isEmpty()) {
                    String category = cells.get(0).text().trim(); //카테고리 수집
                    MenuCategory menuCategory = mapCategory(category); // 카테고리 변환

                    if (menuCategory == null) {
                        log.warn("정의되지 않은 카테고리: {}", category);
                        continue; // 변환되지 않은 카테고리는 무시
                    }

                    Element menuCell = row.selectFirst("td.alignL"); //메뉴가 있는 class
                    List<String> menuContent = menuCell != null
                            ? Arrays.stream(menuCell.html().split("<br>")) // 줄바꿈 기준으로 분리
                            .map(String::trim) // 양쪽 공백 제거
                            .map(content -> content.replace("&amp;", "&")) // &amp;를 &로 변환
                            .toList() // 리스트로 변환
                            : Collections.singletonList("등록된 식단 내용이 없습니다."); // 메뉴 수집

                    if (menuContent.isEmpty()){
                        log.error("메뉴 데이터가 비어 있습니다.");
                    }
                    WeeklyMenu weeklyMenu = WeeklyMenu.create(currentDate, menuCategory, menuContent);
                    weeklyMenus.add(weeklyMenu);
                    menuRepository.save(weeklyMenu);
                    //save() : 영속성 컨텍스트의 cache에 먼저 저장 -> 나중에 flush()
                    //vs. saveAndFlush() : 즉시 db에 반영
                }
            }
            if (weeklyMenus.isEmpty()){
                throw new WeeklyMenuNotFoundException("크롤링된 데이터가 없습니다.", ErrorCode.WEEKLYMENU_NOT_FOUND);
            }

        } catch (Exception e) {
            log.error("크롤링 오류 = {}", e.getMessage(), e);
        }
        return WeeklyMenuResponseDTO.fromEntityToList(weeklyMenus);
    }

    private MenuCategory mapCategory(String category) {
        switch (category) {
            case "조식":
                return MenuCategory.BREAKFAST; // Enum에 정의된 값으로 매핑
            case "중식":
                return MenuCategory.LUNCH; // Enum에 정의된 값으로 매핑
            case "석식":
                return MenuCategory.DINNER; // Enum에 정의된 값으로 매핑
            default:
                return null; // 매핑되지 않은 값은 null 반환
        }
    }
}

