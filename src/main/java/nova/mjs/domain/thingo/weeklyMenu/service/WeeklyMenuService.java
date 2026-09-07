package nova.mjs.domain.thingo.weeklyMenu.service;

import lombok.extern.log4j.Log4j2;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.domain.thingo.weeklyMenu.DTO.WeeklyMenuResponseDTO;
import nova.mjs.domain.thingo.weeklyMenu.entity.WeeklyMenu;
import nova.mjs.domain.thingo.weeklyMenu.entity.enumList.MenuCategory;
import nova.mjs.domain.thingo.weeklyMenu.event.WeeklyMenuCrawledEvent;
import nova.mjs.domain.thingo.weeklyMenu.exception.WeeklyMenuNotFoundException;
import nova.mjs.domain.thingo.weeklyMenu.repository.WeeklyMenuRepository;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class WeeklyMenuService {
    //1. 요청값이 뭔지(파라미터, request body) - 없음
    //2. 요청값으로 뭘 할건지 -> 없는데 뭘 합니까
    //3. 응답값이 뭔지 -> 날짜, 카테고리, 메뉴(리스트)
    //4. db에 저장할지 판단 -> 필요하면 엔티티에 있는 메서드로 객체 생성 : 날짜, 카테고리, 메뉴(리스트)
    //5. 레퍼에 접근해서 엔티티 값을 넣어줘

    private final WeeklyMenuRepository menuRepository;
    private final ApplicationEventPublisher eventPublisher;

    public WeeklyMenuService(WeeklyMenuRepository menuRepository, ApplicationEventPublisher eventPublisher) {
        this.menuRepository = menuRepository;
        this.eventPublisher = eventPublisher;
    }

    // URL 상수 선언
    private static final String url = "https://www.mju.ac.kr/mjukr/8595/subview.do";

    @Transactional
    public List<WeeklyMenuResponseDTO> crawlWeeklyMenu() {
        List<WeeklyMenu> weeklyMenus = new ArrayList<>();

        try {
            weeklyMenus = parseWeeklyMenus(fetchMenuDocument(LocalDate.now()));

            //save() : 영속성 컨텍스트의 cache에 먼저 저장 -> 나중에 flush()
            //vs. saveAndFlush() : 즉시 db에 반영
            if (!weeklyMenus.isEmpty()){
                deleteAllWeeklyMenus();
                log.info("기존 식단 데이터를 삭제했습니다.");

                menuRepository.saveAll(weeklyMenus);
                log.info("새로운 식단 데이터를 저장했습니다. 총 {} 개의 메뉴", weeklyMenus.size());

                // 학식 알림: 크롤 성공 이벤트 발행(트랜잭션 커밋 후 알림 도메인이 수신).
                // 같은 주 반복 크롤링 시 중복 알림은 수신측이 contentSignature 로 판별한다.
                eventPublisher.publishEvent(
                        new WeeklyMenuCrawledEvent(weeklyMenus.size(), buildContentSignature(weeklyMenus)));
            } else{
                log.warn("크롤링된 식단이 없어(미게시 주 포함) 기존 데이터를 삭제하지 않았습니다.");
            }

        } catch (Exception e) {
            log.error("크롤링 오류 = {}", e.getMessage(), e);
        }
        return WeeklyMenuResponseDTO.fromEntityToList(weeklyMenus);
    }

    /**
     * 크롤링 대상 주의 식단 페이지를 가져온다.
     *
     * - 평일: 학교 기본 페이지(이번 주)를 그대로 GET.
     * - 주말: 이번 주 월요일 + week=next 로 POST 해서 다음 주 페이지를 직접 요청한다.
     *   (주말 크롤 시점에는 이번 주가 이미 끝났으므로 다음 주를 보여줘야 한다.)
     */
    private Document fetchMenuDocument(LocalDate today) throws IOException {
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
            return Jsoup.connect(url).get();
        }

        LocalDate currentWeekMonday = today.minusDays(dayOfWeek == DayOfWeek.SATURDAY ? 5 : 6);
        String mondayParam = currentWeekMonday.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));

        log.info("주말이므로 다음 주 식단 페이지를 직접 요청합니다. monday={}, week=next", mondayParam);

        return Jsoup.connect("https://www.mju.ac.kr/diet/mjukr/10/view.do")
                .data("monday", mondayParam)
                .data("week", "next")
                .post();
    }

    /**
     * 식단 표를 파싱해 끼니별 엔티티로 변환한다.
     *
     * 미게시 주 방어
     * - 학교 페이지는 아직 식단이 올라오지 않은 주에도 날짜 뼈대(월~금 x 조/중/석 = 15행)를 그대로 렌더하고,
     *   실제 메뉴 셀(td.alignL)만 통째로 빠진 채 "등록된 식단내용이(가) 없습니다."만 채워서 내려준다.
     * - 이걸 정상 데이터로 취급하면 호출부가 기존 식단을 전부 지우고 빈 껍데기를 저장한다.
     * - 그래서 표 전체에 메뉴 셀이 하나도 없으면 빈 목록을 돌려주고, 기존 데이터를 그대로 유지하게 한다.
     */
    List<WeeklyMenu> parseWeeklyMenus(Document doc) {
        List<WeeklyMenu> weeklyMenus = new ArrayList<>();

        Element tableWrap = doc.selectFirst(".tableWrap.marT50");
        if (tableWrap == null) {
            log.error("테이블을 포함하는 div를 찾을 수 없습니다.");
            throw new WeeklyMenuNotFoundException("식단 데이터를 찾을 수 없습니다.", ErrorCode.WEEKLYMENU_NOT_FOUND);
        }

        Element table = tableWrap.selectFirst("table");
        if (table == null) {
            log.error("테이블을 찾을 수 없습니다.");
            throw new WeeklyMenuNotFoundException("식단 데이터를 찾을 수 없습니다.", ErrorCode.WEEKLYMENU_NOT_FOUND);
        }

        Elements rows = table.select("tbody tr");
        if (rows.isEmpty()){
            log.error("식단 데이터를 찾을 수 없습니다.");
            throw new WeeklyMenuNotFoundException("식단 데이터를 찾을 수 없습니다.", ErrorCode.WEEKLYMENU_NOT_FOUND);
        }

        // 아직 식단이 게시되지 않은 주 -> 기존 데이터를 지키기 위해 빈 목록 반환
        if (table.selectFirst("td.alignL") == null) {
            log.warn("아직 식단이 게시되지 않은 주입니다. 기존 식단을 유지합니다. 행 수={}", rows.size());
            return weeklyMenus;
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
            }
        }

        return weeklyMenus;
    }

    /**
     * 크롤된 식단 전체의 내용 지문(날짜+끼니+메뉴 기반).
     * 같은 주를 반복 크롤링하면 동일 값이 나와 수신측이 중복 알림을 건너뛸 수 있다.
     */
    private String buildContentSignature(List<WeeklyMenu> weeklyMenus) {
        String joined = weeklyMenus.stream()
                .map(menu -> menu.getDate() + "|" + menu.getMenuCategory() + "|" + String.join(",", menu.getMeals()))
                .sorted()
                .collect(Collectors.joining(";"));
        return Integer.toHexString(joined.hashCode());
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

    //식단을 크롤링했을 때 중복 발생을 고려한 식단 데이터 삭제하는 메서드
    @Transactional
    public void deleteAllWeeklyMenus(){
        menuRepository.deleteAll();
    }

    //DB에서 해당 주 전체 식단 데이터를 가져오는 메서드 (크롤링 시점 기준 1주일치)
    public List<WeeklyMenuResponseDTO> getAllWeeklyMenus() {
        // 크롤링 순서(월~금, 조/중/석)를 보존하기 위해 id 오름차순 정렬
        List<WeeklyMenu> menus = menuRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        if (menus.isEmpty()) {
            throw new WeeklyMenuNotFoundException("저장된 식단 정보가 없습니다.", ErrorCode.WEEKLYMENU_NOT_FOUND);
        }

        return WeeklyMenuResponseDTO.fromEntityToList(menus);
    }
}

