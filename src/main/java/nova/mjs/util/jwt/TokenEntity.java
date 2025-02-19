package nova.mjs.util.jwt;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;  // 사용자 이메일 (식별자)

    @Column(nullable = false, unique = true)
    private String refreshToken;  // Refresh Token 값
}
