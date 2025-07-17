package nova.mjs.domain.calendar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.calendar.dto.MjuCalendarDTO;
import nova.mjs.domain.calendar.entity.MjuCalendar;
import nova.mjs.domain.calendar.repository.MjuCalendarRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MjuCalendarService {

    private static final String BASE =
            "https://www.mju.ac.kr/schdulmanage/mjukr/4/yearSchdul.do";
    private static final Pattern DATE_RGX =
            Pattern.compile("\\.(\\d{2}) \\.(\\d{2})");

    private final RestTemplate restTemplate;
    private final MjuCalendarRepository calendarRepository;


    public Page<MjuCalendarDTO> getCalendarsFiltered(Integer year, Pageable pageable) {
        Page<MjuCalendar> result;

        if (year != null) {
            result = calendarRepository.findByYear(year, pageable);
        } else {
            result = calendarRepository.findAll(pageable);
        }

        return result.map(MjuCalendarDTO::fromEntity);
    }

    @Transactional
    public void refresh(int fromYear, int toYear) {
        calendarRepository.deleteAll();  // 전체 초기화 후 재갱신

        for (int y = fromYear; y <= toYear; y++) {
            List<MjuCalendarDTO> list = crawlYear(y);
            list.forEach(dto -> calendarRepository.save(MjuCalendar.create(dto)));
            log.info("{}학년도 일정 {}건 저장 완료", y, list.size());
        }
    }

    private List<MjuCalendarDTO> crawlYear(int year) {
        String html = restTemplate.getForObject(BASE + "?year=" + year, String.class);
        Document doc = Jsoup.parse(html);

        List<MjuCalendarDTO> result = new ArrayList<>();

        for (Element li : doc.select("#timeTableList li")) {
            Element strong = li.selectFirst("dl dt strong");
            if (strong == null) continue;

            for (Element item : li.select("dd .text-list li")) {
                Element dateEl = item.selectFirst("strong");
                if (dateEl == null) continue;

                String dateRange = dateEl.text().trim();
                LocalDate[] dates = parseDateRange(year, dateRange);
                if (dates == null) continue;

                String desc = item.text().replace(dateRange, "").trim();
                result.add(new MjuCalendarDTO(year, dates[0], dates[1], desc));
            }
        }

        return result;
    }

    /** “.MM .dd ~ .MM .dd” 또는 “.MM .dd” 를 LocalDate[2] 로 변환 */
    private LocalDate[] parseDateRange(int academicYear, String s) {
        Matcher m = DATE_RGX.matcher(s);
        List<int[]> parts = new ArrayList<>();
        while (m.find()) {
            parts.add(new int[] {
                    Integer.parseInt(m.group(1)),   // month
                    Integer.parseInt(m.group(2))    // day
            });
        }
        if (parts.isEmpty()) return null;

        LocalDate start = LocalDate.of(academicYear, parts.get(0)[0], parts.get(0)[1]);
        LocalDate end;

        if (parts.size() == 1) {
            end = start;
        } else {
            int endMonth = parts.get(1)[0];
            int endDay = parts.get(1)[1];
            int endYear = (endMonth < start.getMonthValue()) ? academicYear + 1 : academicYear;
            end = LocalDate.of(endYear, endMonth, endDay);
        }

        return new LocalDate[] { start, end };
    }
}
