package nova.mjs.util.ElasticSearch.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.thingo.department.entity.DepartmentSchedule;
import nova.mjs.util.ElasticSearch.SearchType;
import nova.mjs.util.ElasticSearch.config.KomoranTokenizerUtil;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Document(indexName = "department_schedule_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentScheduleDocument implements SearchDocument{

    @Id
    private String id;

    private String title;

    private String content;

    private String department;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant date;

    @CompletionField
    private List<String> suggest;

    private String type;

    @Override
    public String getType() {
        return SearchType.DEPARTMENT_SCHEDULE.name();
    }

    @Override
    public LocalDateTime getDate() {
        return date != null
                ? date.atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;
    }
    public static DepartmentScheduleDocument from(DepartmentSchedule schedule) {
        return DepartmentScheduleDocument.builder()
                .id(schedule.getDepartmentScheduleUuid().toString())
                .title(schedule.getTitle())
                .content(schedule.getContent())
                .department(schedule.getDepartment().getDepartmentName().getLabel())
                .date(schedule.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant())
                .suggest(KomoranTokenizerUtil.generateSuggestions(schedule.getTitle()))
                .type(SearchType.DEPARTMENT_SCHEDULE.name())
                .build();
    }
}
