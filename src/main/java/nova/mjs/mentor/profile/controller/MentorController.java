package nova.mjs.mentor.profile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.mentor.profile.dto.MentorProfileDTO;
import nova.mjs.mentor.profile.dto.MentorRegistrationDTO;
import nova.mjs.mentor.profile.entity.Mentor;
import nova.mjs.mentor.profile.service.command.MentorProfileCommandService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.AuthDTO;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorProfileCommandService mentorProfileCommandService;

    /**
     * 신규 회원가입 + 멘토 프로필 동시 등록
     * - 응답: 액세스/리프레시 토큰
     */
    @PostMapping("")
    public ResponseEntity<ApiResponse<AuthDTO.LoginResponseDTO>> registerMemberAndMentor(
            @RequestBody @Valid MentorRegistrationDTO.Request request
    ) {
        AuthDTO.LoginResponseDTO tokens = mentorProfileCommandService.registerMemberAndMentor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(tokens));
    }

    /**
     * 기존 회원에 멘토 프로필 추가 (로그인 필요)
     * - 응답: 멘토 프로필 요약(Response)
     */
    @PostMapping("conversion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorProfileDTO.Response>> addMentorProfileForExistingMember(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid MentorProfileDTO.Request request
    ) {
        String email = userPrincipal.getUsername(); // 프로젝트에서 username=이메일 사용
        Mentor mentor = mentorProfileCommandService.addMentorProfileForExistingMember(email, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MentorProfileDTO.Response.fromEntity(mentor)));
    }
}
