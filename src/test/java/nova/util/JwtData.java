package nova.util;

import nova.mjs.util.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtData {

    @Autowired
    JwtUtil jwtUtil;

    public String generateTestAccessToken() {
        UUID uuid = UUID.randomUUID();
        String email = "mjs@mju.ac.kr";
        String role = "USER";
        return jwtUtil.generateAccessToken(uuid, email, role);
    }
}
