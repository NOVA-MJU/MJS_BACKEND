package nova.mjs.domain.mentorship.ElasticSearch.Document;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.thingo.calendar.entity.MjuCalendar;
import nova.mjs.domain.mentorship.ElasticSearch.SearchType;
import nova.mjs.config.elasticsearch.KomoranTokenizerUtil;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;


@Document(indexName = "mju_calendar_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MjuCalendarDocument implements SearchDocument  {

    @Id
    private String id;

    private String title;

    private String content;

    @CompletionField
    private List<String> suggest;

    private String type;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant date;

    @Override
    public String getType() {
        return SearchType.MJU_CALENDAR.name();
    }

    @Override
    public LocalDateTime getDate() {
        return date != null
                ? date.atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;
    }

    public static MjuCalendarDocument from(MjuCalendar mjuCalendar) {
        return MjuCalendarDocument.builder()
                .id(mjuCalendar.getId().toString())
                .title(mjuCalendar.getDescription())
                .content("")
                .date(mjuCalendar.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant())
                .suggest(KomoranTokenizerUtil.generateSuggestions(mjuCalendar.getDescription()))
                .type(SearchType.MJU_CALENDAR.name())
                .build();
    }
}
