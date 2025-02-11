package nova.mjs.mypage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileRequest {
    private String nickname;       // 수정할 닉네임
    private String profileImage;   // 수정할 프로필 이미지 URL
}
