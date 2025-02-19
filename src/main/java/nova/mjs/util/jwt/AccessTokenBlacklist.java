package nova.mjs.util.jwt;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accessToken; // 블랙리스트된 토큰 저장

    public AccessTokenBlacklist(String accessToken) {
        this.accessToken = accessToken;
    }
}