package nova.mjs.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class UserProfileDto {
    private String nickname;
    private String email;
    private String profileImage;

    public static UserProfileDto fromEntity(nova.mjs.mypage.entity.User user) {
        return UserProfileDto.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .build();
    }
}
