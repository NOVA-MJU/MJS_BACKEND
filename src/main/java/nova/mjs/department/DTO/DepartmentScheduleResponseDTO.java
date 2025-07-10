package nova.mjs.department.DTO;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.department.entity.Department;
import nova.mjs.department.entity.DepartmentSchedule;
import nova.mjs.department.repository.DepartmentScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class DepartmentScheduleResponseDTO { //list여야 함
    private DepartmentInfoDTO departmentInfo;
    private List<ScheduleSimpleDTO> schedules;

    public static DepartmentScheduleResponseDTO fromScheduleList(Department department, List<DepartmentSchedule> scheduleEntities) {
        return DepartmentScheduleResponseDTO.builder()
                .departmentInfo(DepartmentInfoDTO.fromDepartmentEntity(department))
                .schedules(ScheduleSimpleDTO.fromList(scheduleEntities))
                .build();
    }
    @Getter
    @Builder
    public static class ScheduleSimpleDTO{
        private UUID departmentScheduleUuid;
        private String title;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private String content;

        public static ScheduleSimpleDTO fromScheduleEntity(DepartmentSchedule schedule) {
            return ScheduleSimpleDTO.builder()
                    .departmentScheduleUuid(schedule.getDepartmentScheduleUuid())
                    .title(schedule.getTitle())
                    .startDateTime(schedule.getStartDate())
                    .endDateTime(schedule.getEndDate())
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
