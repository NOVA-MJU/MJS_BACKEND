package nova.mjs.domain.calendar.dto;

import nova.mjs.domain.calendar.entity.MjuCalendar;

import java.time.LocalDate;

public record MjuCalendarDTO(
        int year,
        LocalDate startDate,
        LocalDate endDate,
        String description
) {
    public static MjuCalendarDTO fromEntity(MjuCalendar entity) {
        return new MjuCalendarDTO(
                entity.getYear(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getDescription());
    }
}
