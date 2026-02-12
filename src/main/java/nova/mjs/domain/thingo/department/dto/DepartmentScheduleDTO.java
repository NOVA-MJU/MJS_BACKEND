package nova.mjs.domain.thingo.department.dto;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.department.entity.DepartmentSchedule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 학과 일정 응답 DTO
 *
 * 구조:
 *  - Response
 *      - List<Schedule>
 */
public class DepartmentScheduleDTO {

    @Getter
    @Builder
    public static class Response {
        private List<Schedule> schedules;

        public static Response from(List<DepartmentSchedule> entities) {
            return Response.builder()
                    .schedules(
                            entities.stream()
                                    .map(Schedule::from)
                                    .toList()
                    )
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Schedule {

        private String uuid;
        private String title;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private String content;

        public static Schedule from(DepartmentSchedule entity) {
            return Schedule.builder()
                    .uuid(entity.getDepartmentScheduleUuid().toString())
                    .title(entity.getTitle())
                    .startDateTime(entity.getStartDate().atStartOfDay())
                    .endDateTime(entity.getEndDate().atStartOfDay())
                    .content(entity.getContent())
                    .build();
        }
    }
}
