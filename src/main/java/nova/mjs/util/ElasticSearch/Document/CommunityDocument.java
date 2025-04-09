package nova.mjs.util.ElasticSearch.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.community.entity.CommunityBoard;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Document(indexName = "community_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityDocument implements SearchDocument {

    @Id
    private String id;

    private String title;

    private String content;

    private String date;

    private String type;

    private String link;

    @Override
    public String getType() {
        return "Community";
    }

    public static CommunityDocument from(CommunityBoard board) {
        return CommunityDocument.builder()
                .id(String.valueOf(board.getId()))
                .title(board.getTitle())
                .content(board.getContent())
                .date(board.getPublishedAt().toString())
                .type("community")
                .build();
    }
}
