package nova.mjs.util.ElasticSearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDTO {
    private String id;
    private String highlightedTitle;
    private String highlightedContent;
    private String date;
    private String link;
    private String type;
}