package nova.mjs.domain.thingo.member.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileCountResponse {
    private String nickname;
    private int postCount;
    private int commentCount;
    private int likedPostCount;
    private long mapFavoriteCount;
    private long keywordAlarmCount;
    private long blockedUserCount;
}
