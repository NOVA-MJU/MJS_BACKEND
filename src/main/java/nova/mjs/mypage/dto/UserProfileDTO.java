package nova.mjs.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nova.mjs.mypage.entity.User;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private String nickname;
    private String email;
    private String profileImage;

    public static UserProfileDTO fromEntity(User user) {
        return UserProfileDTO.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .build();
    }
}
