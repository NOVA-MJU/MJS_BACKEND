package nova.mjs.domain.calendar.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.admin.account.exception.AdminIdMismatchWithDepartmentException;
import nova.mjs.domain.calendar.dto.MjuCalendarDTO;
import nova.mjs.domain.calendar.entity.MjuCalendar;
import nova.mjs.domain.calendar.repository.MjuCalendarRepository;
import nova.mjs.domain.calendar.service.MjuCalendarService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
@Slf4j
public class MjuCalendarController {

    private final MjuCalendarService calendarService;
    private final MjuCalendarRepository calendarRepository;

    /** 학사일정 전체 수집 및 저장 (크롤링) */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshCalendar(
            @RequestParam(defaultValue = "2020") int from,
            @RequestParam(defaultValue = "2026") int to
    ) {
        calendarService.refresh(from, to);
        return ResponseEntity.ok("학사일정 수집 완료: " + from + " ~ " + to);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MjuCalendarDTO>>> getAllCalendars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Integer year
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MjuCalendarDTO> calendars = calendarService.getCalendarsFiltered(year, pageable);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(calendars));
    }

    // 월별 교차 포함 조회 + 카테고리 분류(전체/학부/대학원/휴일)
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MjuCalendarDTO.MonthlyResponse>> getMonthly(
            @RequestParam int year,
            @RequestParam int month
    ) {
        MjuCalendarDTO.MonthlyResponse body = calendarService.getMonthlyByYearAndMonth(year, month);
        return ResponseEntity.ok(ApiResponse.success(body));
    }
}
