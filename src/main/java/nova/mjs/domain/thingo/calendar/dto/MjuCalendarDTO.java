package nova.mjs.domain.thingo.calendar.dto;

import lombok.*;
import nova.mjs.domain.thingo.calendar.entity.MjuCalendar;

import java.time.LocalDate;
import java.util.List;

// MjuCalendarDTO.java (기존 DTO 유지 + 아래 내부 타입 추가)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MjuCalendarDTO {
    private int year;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    public static MjuCalendarDTO fromEntity(MjuCalendar entity) {
        return new MjuCalendarDTO(
                entity.getYear(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getDescription());
    }

    @Getter @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MonthlyResponse {
        private int year;
        private int month;
        private List<Category> all;
        private List<Category> undergrad;
        private List<Category> graduate;
        private List<Category> holiday;
    }

    // [추가] 응답 카테고리 아이템
    @Getter @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Category {
        private Long id;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;  // 선두 대괄호 등 기호 제거, <br> 정리
//        private Set<String> rawTags; // 학부/대학원 파싱 결과
//        private Set<String> matchedRules; // 휴일 규칙 매칭 결과(디버깅용)
    }
}
