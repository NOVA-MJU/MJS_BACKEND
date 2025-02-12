package nova.mjs.util.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(nullable = false, unique = true)
    private String userId;  // 사용자 ID (로그인 ID)

    @Column(nullable = false)
    private String password;  // 비밀번호 (암호화된 상태로 저장)

    @Column(nullable = false, unique = true)
    private String email;  // 이메일

    @Column(nullable = false)
    private String fullName;  // 사용자 이름

    @Column(nullable = false)
    private String role;  // ROLE_USER, ROLE_ADMIN 등의 권한
}
