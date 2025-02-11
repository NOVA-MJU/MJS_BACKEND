package nova.mjs.mypage.service;

import nova.mjs.mypage.exception.UserNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.mypage.dto.UserProfileDto;
import nova.mjs.mypage.dto.UserProfileRequest;
import nova.mjs.mypage.entity.User;
import nova.mjs.mypage.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;

    /**
     * 프로필 조회 트렌젝션 어노테이션이 없다
     */
    public UserProfileDto getUserProfile() {
        User currentUser = getCurrentUser();
        return new UserProfileDto.fromEntity(currentUser);
    }

    /**
     * 프로필 수정
     */
    public void updateUserProfile(UserProfileRequest profileRequest) {
        User currentUser = getCurrentUser();

        currentUser.updateProfile(profileRequest.getNickname(), profileRequest.getProfileImage());
        userRepository.save(currentUser);

        log.info("user profile updated: {}", currentUser.getEmail());
    }

    /**
     * 현재 인증된 사용자 가져오기
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", email);
                    // 기존 글로벌 예외 처리기를 활용하기 위해 UsernameNotFoundException 사용

                    return new UserNotFoundException();
                });
    }
}
