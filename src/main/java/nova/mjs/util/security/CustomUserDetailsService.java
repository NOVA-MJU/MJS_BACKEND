package nova.mjs.util.security;

import lombok.RequiredArgsConstructor;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일을 이용해 사용자 정보를 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        // Spring Security에서 사용할 UserDetails 객체 반환
        return new User(
                member.getEmail(),  // 사용자의 이메일
                member.getPassword(), // 저장된 해시된 비밀번호
                Collections.emptyList() // 권한 (추후 설정 가능)
        );
    }

    public UserDetails loadUserByUuid(UUID uuid) {
        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + uuid));

        return new User(
                member.getEmail(),
                member.getPassword(),
                Collections.emptyList()
        );
    }
}
