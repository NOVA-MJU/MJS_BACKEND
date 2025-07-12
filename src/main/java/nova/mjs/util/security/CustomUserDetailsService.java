package nova.mjs.util.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일을 이용해 사용자 정보를 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다: {}", email);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
                });
        log.info("사용자 조회 성공: {}", member.getEmail());

        // Spring Security에서 사용할 UserDetails 객체 반환
        return UserPrincipal.fromMember(member);
    }

    public UserDetails loadUserByUuid(UUID uuid) {
        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + uuid));

        return UserPrincipal.fromMember(member);
    }
}
