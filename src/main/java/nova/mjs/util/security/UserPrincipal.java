package nova.mjs.util.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final UUID uuid;
    private final String email;
    private final String password;
    private final String fullName;
    private final String role;

    public UserPrincipal(String email, UUID uuid, String password, String fullName, String role) {
        this.email = email;
        this.uuid = uuid;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public UserPrincipal(UUID uuid, String email, String role) {
        this.email = email;
        this.role = role;
        this.uuid = uuid;
        this.password = null;
        this.fullName = null;
    }

    public UserPrincipal(String email, String role) {
        this.email = email;
        this.role = role;
        this.uuid = null;
        this.password = null;
        this.fullName = null;
    }

    public static UserPrincipal fromMember(Member member){
        return new UserPrincipal(
                member.getEmail(),
                member.getUuid(),
                member.getPassword(),
                member.getName(),
                String.valueOf(member.getRole())
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
