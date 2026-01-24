package nova.mjs.config.webSocket;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.member.repository.MemberRepository;
import nova.mjs.util.jwt.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // CONNECT 시점에만 인증/Principal 세팅 (이후 SEND/SUBSCRIBE는 principal을 그대로 사용)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 1) 헤더에서 Authorization 꺼내기 (STOMP native header)
            String bearer = accessor.getFirstNativeHeader("Authorization");
            String token = jwtUtil.extractToken(bearer);  // "Bearer " 형식 검증 포함
            jwtUtil.validateToken(token);                 // 만료/위변조 검증

            // 2) JWT subject(UUID) 추출
            UUID uuid = jwtUtil.getUserIdFromToken(token);
            if (uuid == null) {
                throw new IllegalArgumentException("JWT subject(uuid)가 비어있습니다.");
            }

            // 3) UUID -> Long userId로 변환 (DB 조회)
            Long userId = memberRepository.findIdByUuid(uuid)
                    .orElseThrow(() -> new IllegalArgumentException("해당 uuid의 사용자를 찾을 수 없습니다: " + uuid));

            // 4) Principal 세팅: principal.getName() == "userId"
            accessor.setUser(new StompPrincipal(String.valueOf(userId)));
        }

        return message;
    }

    // Principal을 직접 구현해서 name에 userId 문자열만 담는다
    static class StompPrincipal implements Principal {
        private final String name;
        StompPrincipal(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }
}
