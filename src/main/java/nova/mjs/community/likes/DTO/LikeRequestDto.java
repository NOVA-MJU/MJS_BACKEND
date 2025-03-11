package nova.mjs.community.likes.DTO;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeRequestDto {
    private UUID memberUUID;
    private UUID boardUUID;

    public LikeRequestDto(UUID memberUUID, UUID boardUUID) {
        this.memberUUID = memberUUID;
        this.boardUUID = boardUUID;
    }
}
