package nova.mjs.domain.mentorship.ElasticSearch.Document;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.thingo.department.entity.DepartmentNotice;
import nova.mjs.domain.mentorship.ElasticSearch.SearchType;
import nova.mjs.config.elasticsearch.KomoranTokenizerUtil;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Document(indexName = "department_notice_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentNoticeDocument implements SearchDocument{

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
        return SearchType.DEPARTMENT_NOTICE.name();
    }

     @Override
    public Instant getInstant() {
        return date;
    }

    public static DepartmentNoticeDocument from(DepartmentNotice departmentNotice) {
        return DepartmentNoticeDocument.builder()
                .id(departmentNotice.getUuid().toString())
                .title(departmentNotice.getTitle())
                .content(departmentNotice.getContent())
                .department(departmentNotice.getDepartment().getDepartmentName().getLabel())
                .date(departmentNotice.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())
                .suggest(KomoranTokenizerUtil.generateSuggestions(departmentNotice.getTitle()))
                .type(SearchType.DEPARTMENT_NOTICE.name())
                .build();
    }
}
