package nova.mjs.util.ElasticSearch.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.notice.entity.Notice;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "notice_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDocument implements SearchDocument{

    @Id
    private String id;

    private String title;

    private String content; // date나 category를 문자열로 통합해서 넣어도 좋음

    private String type;

    @Override
    public String getType() {
        return "Notice";
    }

    public static NoticeDocument from(Notice notice) {
        return NoticeDocument.builder()
                .id(notice.getId().toString())
                .title(notice.getTitle())
                .content("") // Notice는 content 없으면 빈 문자열 처리
                .type("notice")
                .build();
    }

}