package nova.mjs.domain.thingo.member.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailVerificationResultDto {
    private String email;
    private boolean matched;
}
