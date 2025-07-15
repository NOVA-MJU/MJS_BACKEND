package nova.mjs.util.ElasticSearch.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.community.entity.CommunityBoard;
import nova.mjs.domain.department.entity.DepartmentNotice;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    private String type;

    private String department;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant date;

    @Override
    public String getType() {
        return "DepartmentNotice";
    }

    @Override
    public LocalDateTime getDate() {
        return date != null
                ? date.atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;
    }

    public static DepartmentNoticeDocument from(DepartmentNotice notice) {
        return DepartmentNoticeDocument.builder()
                .id(notice.getUuid().toString())
                .title(notice.getTitle())
                .content(notice.getContent())
                .type("DepartmentNotice")
                .department(notice.getDepartment().getDepartmentName().getLabel())
                .date(notice.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())
                .build();
    }
}
