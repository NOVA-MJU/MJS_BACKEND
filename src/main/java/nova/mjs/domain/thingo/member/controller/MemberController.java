package nova.mjs.domain.thingo.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.member.DTO.MemberDTO;
import nova.mjs.domain.thingo.member.email.EmailVerificationRequestDto;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.service.command.MemberCommandService;
import nova.mjs.domain.thingo.member.service.query.MemberQueryService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.AuthDTO;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;

    // 1. GET 페이지네이션
    @GetMapping
    public  ResponseEntity<ApiResponse<Page<MemberDTO>>> getAllMembers(
            @RequestParam(defaultValue = "0") int page, // 기본 페이지 번호
            @RequestParam(defaultValue = "10") int size // 기본 페이지 크기
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MemberDTO> boards = memberQueryService.getAllMember(pageable);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(boards));
    }


    // 회원 정보 조회
    @GetMapping("info")
    @PreAuthorize("isAuthenticated() and (#userPrincipal.email == principal.username or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<MemberDTO>> getMember(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        MemberDTO member = memberQueryService.getMemberDtoByEmailId(userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(member));
    }

    /** ---------------------------------------------------------------
     * 회원가입에 필요한 로직
     * 1. 이메일 중복 검증, 2. 이메일 인증/검증(EmailService)
     * 3. 프로필 이미지 생성, 4. 닉네임 중복 체크, 5. 회원정보 생성
     * ----------------------------------------------------------------- */

    // 회원 정보 생성 (회원 가입)
    @PostMapping
    public ResponseEntity<ApiResponse<?>> registerMember(@Validated @RequestBody MemberDTO.MemberRegistrationRequestDTO requestDTO) {
        // 사용자로부터 입력받은 file을 s3 이미지로 저장한 후 url을 update 메서드로 둘것.
        AuthDTO.LoginResponseDTO newMember = memberCommandService.registerMember(requestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(newMember));
    }

    /**
     * 프로필 이미지 생성: 프로필 이미지 업로드 후 CloudFront URL 반환
     *
     * @param file MultipartFile
     * @return CloudFront로 접근 가능한 이미지 URL
     */
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        log.info("프로필 이미지 업로드 요청: {}", file.getOriginalFilename());
        String imageUrl = memberCommandService.uploadProfileImage(file);
        return ResponseEntity.ok(ApiResponse.success(imageUrl));
    }

    // 회원 정보 수정
    @PatchMapping("/info")
    @PreAuthorize("isAuthenticated() and (#userPrincipal.email == principal.username or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<MemberDTO>> updateMember(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                               @RequestBody MemberDTO.MemberUpdateRequestDTO requestDTO) {
        Member updatedMember = memberCommandService.updateMember(userPrincipal.getUsername(), requestDTO);
        return ResponseEntity.ok(ApiResponse.success(MemberDTO.fromEntity(updatedMember)));
    }

    // 비밀번호 변경
    @PatchMapping("/info/password")
    @PreAuthorize("isAuthenticated() and (#userPrincipal.email == principal.username or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<String>> updatePassword(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                              @RequestBody MemberDTO.PasswordRequestDTO request) {
        memberCommandService.updatePassword(userPrincipal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경이 완료되었습니다."));
    }

    // 회원 정보 삭제
    @DeleteMapping("/info")
    @PreAuthorize("isAuthenticated() and (#userPrincipal.email == principal.username or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<String>> deleteMember(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                          @RequestBody MemberDTO.PasswordRequestDTO password) {
        memberCommandService.deleteMember(userPrincipal.getUsername(), password);
        return ResponseEntity.ok(ApiResponse.success("회원 정보가 삭제되었습니다."));
    }

    // 이메일 중복 검증 (회원가입)
    @GetMapping("/validation/email")
    public ResponseEntity<ApiResponse<String>> validateEmail(@RequestParam String email) {
        memberQueryService.validateEmailDomain(email);
        memberQueryService.validateEmailDuplication(email);
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 이메일입니다."));
    }


    // 닉네임 중복 검증
    @GetMapping("/validation/nickname")
    public ResponseEntity<ApiResponse<String>> validateNickname(@RequestParam String nickname) {
        memberQueryService.validateNicknameDuplication(nickname);
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 닉네임입니다."));
    }

    // 학번 중복 검증
    @GetMapping("/validation/student-number")
    public ResponseEntity<ApiResponse<String>> validateStudentNumber(@RequestParam String studentNumber) {
        memberQueryService.validateStudentNumberDuplication(studentNumber);
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 학번입니다."));
    }


    // == RECOVERY - 회원 정보 찾기 == //
    // 비밀번호 찾기
    /**
     * 1단계: 이메일 보내기 - EMAIL CONTROLLER 에서 진행
     * 2단계: 코드 검증 성공 시 내부 플래그 세팅
     */
    @PostMapping("/recovery/verify-code")
    public ResponseEntity<ApiResponse<String>> verifyCodeForRecovery(
            @RequestBody EmailVerificationRequestDto request) {
        memberCommandService.verifyCodeForRecovery(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다."));
    }

    @PostMapping("/recovery/reset")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestBody MemberDTO.PasswordResetRequestDTO request) {
        memberCommandService.resetPasswordAfterVerified(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 재설정되었습니다."));
    }
}

