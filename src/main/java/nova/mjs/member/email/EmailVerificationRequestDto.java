package nova.mjs.member.email;

import lombok.Getter;

@Getter
public class EmailVerificationRequestDto {
    private String email;
    private String code;
}
