package nova.mjs.admin.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminRequestDTO {
    private String department;
    private String studentUnionName;
    private String homepageUrl;
    private String instagramUrl;
    private String introduction;
}