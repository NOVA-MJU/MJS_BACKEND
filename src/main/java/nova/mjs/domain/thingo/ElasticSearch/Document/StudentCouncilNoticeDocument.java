package nova.mjs.domain.thingo.ElasticSearch.Document;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.thingo.department.entity.StudentCouncilNotice;
import nova.mjs.domain.thingo.ElasticSearch.SearchType;
import nova.mjs.config.elasticsearch.KomoranTokenizerUtil;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Document(indexName = "department_notice_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCouncilNoticeDocument implements SearchDocument{

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

    public static StudentCouncilNoticeDocument from(StudentCouncilNotice studentCouncilNotice) {
        return StudentCouncilNoticeDocument.builder()
                .id(studentCouncilNotice.getUuid().toString())
                .title(studentCouncilNotice.getTitle())
                .content(studentCouncilNotice.getContent())
                .department(studentCouncilNotice.getDepartment().getDepartmentName().getLabel())
                .date(studentCouncilNotice.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())
                .suggest(KomoranTokenizerUtil.generateSuggestions(studentCouncilNotice.getTitle()))
                .type(SearchType.DEPARTMENT_NOTICE.name())
                .build();
    }
}
