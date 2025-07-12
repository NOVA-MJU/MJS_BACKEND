package nova.mjs.domain.member.email;

import lombok.Getter;

@Getter
public class EmailVerificationRequestDto {
    private String email;
    private String code;
}
