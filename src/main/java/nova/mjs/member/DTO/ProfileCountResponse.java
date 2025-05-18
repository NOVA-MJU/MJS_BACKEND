package nova.mjs.member.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileCountResponse {
    private String nickname;
    private int postCount;
    private int commentCount;
    private int likedPostCount;
}
