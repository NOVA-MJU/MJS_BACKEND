package nova.mjs.admin.account.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDTO {

    private String adminId;
    private String password;

    @Builder
    public LoginRequestDTO(String adminId, String password) {
        this.adminId = adminId;
        this.password = password;
    }
}