package nova.mjs.mypage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.mypage.dto.UserProfileDto;
import nova.mjs.mypage.dto.UserProfileRequest;
import nova.mjs.mypage.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 프로필 조회
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile() {
        log.info("Request received: Get user profile");
        UserProfileDto userProfile = myPageService.getUserProfile();
        log.info("Response sent: User profile fetched successfully");
        return ResponseEntity.ok(userProfile);
    }

    /**
     * 프로필 수정
     */
    @PatchMapping("/profile")
    public ResponseEntity<String> updateUserProfile(@RequestBody UserProfileRequest profileRequest) {
        log.info("Request received: Update user profile");
        myPageService.updateUserProfile(profileRequest);
        log.info("Response sent: User profile updated successfully");
        return ResponseEntity.ok("프로필이 성공적으로 수정되었습니다.");
    }
}
