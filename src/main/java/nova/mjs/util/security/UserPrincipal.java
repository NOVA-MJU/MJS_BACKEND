package nova.mjs.util.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nova.mjs.util.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final String userId;
    private final String password;
    private final String email;
    private final String fullName;
    private final String role;

    public UserPrincipal(String userId, String role) {
        this.userId = userId;
        this.role = role;
        this.fullName = null;
        this.email = null;
        this.password = null;
    }

    //User 엔티티를 기반으로 UserPrincipal 생성
//    public static UserPrincipal fromEntity(User user) {
//        return new UserPrincipal(
//                user.getUserId(),
//                user.getPassword(),
//                user.getEmail(),
//                user.getFullName(),
//                user.getRole()
//        );
//    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return userId;
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
