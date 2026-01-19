package nova.mjs.domain.thingo.department.dto;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.department.entity.DepartmentSchedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class DepartmentScheduleResponseDTO {

    private List<ScheduleSimpleDTO> schedules;

    public static DepartmentScheduleResponseDTO fromScheduleList(List<DepartmentSchedule> scheduleEntities) {
        return DepartmentScheduleResponseDTO.builder()
                .schedules(ScheduleSimpleDTO.fromList(scheduleEntities))
                .build();
    }

    @Getter
    @Builder
    public static class ScheduleSimpleDTO {
        private UUID departmentScheduleUuid;
        private String title;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private String content;

        public static ScheduleSimpleDTO fromScheduleEntity(DepartmentSchedule schedule) {
            return ScheduleSimpleDTO.builder()
                    .departmentScheduleUuid(schedule.getDepartmentScheduleUuid())
                    .title(schedule.getTitle())
                    .startDateTime(schedule.getStartDate().atStartOfDay())
                    .endDateTime(schedule.getEndDate().atStartOfDay())
                    .content(schedule.getContent())
                    .build();
        }

        public static List<ScheduleSimpleDTO> fromList(List<DepartmentSchedule> schedules) {
            return schedules.stream()
                    .map(ScheduleSimpleDTO::fromScheduleEntity)
                    .toList();
        }
    }
}
