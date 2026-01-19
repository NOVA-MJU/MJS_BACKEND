package nova.mjs.domain.thingo.member.email;

import lombok.Getter;

@Getter
public class EmailVerificationRequestDto {
    private String email;
    private String code;
}
