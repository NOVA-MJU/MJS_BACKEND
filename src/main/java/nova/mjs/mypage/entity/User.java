package nova.mjs.mypage.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;       // 사용자 닉네임
    private String email;          // 사용자 이메일
    private String profileImage;   // 프로필 이미지 URL

    public void updateProfile(String nickname, String profileImage) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if(profileImage != null) {
            this.profileImage = profileImage;
        }
    }
}
