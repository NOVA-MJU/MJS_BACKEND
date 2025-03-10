package nova.mjs.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 1. GET 페이지네이션
    @GetMapping
    public  ResponseEntity<ApiResponse<Page<MemberDTO>>> getAllMembers(
            @RequestParam(defaultValue = "0") int page, // 기본 페이지 번호
            @RequestParam(defaultValue = "10") int size // 기본 페이지 크기
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MemberDTO> boards = memberService.getAllMember(pageable);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(boards));
    }


    // 회원 정보 조회
    @GetMapping()
    @PreAuthorize("isAuthenticated() and ((#userPrincipal.uuid.toString().equals(principal.username)) or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<MemberDTO>> getMember(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        MemberDTO member = memberService.getMemberByEmailId(userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(member));
    }

    // 회원 정보 생성 (회원 가입)
    @PostMapping
    public ResponseEntity<ApiResponse<?>> registerMember(@RequestBody MemberDTO.MemberRequestDTO requestDTO) {
        AuthDTO.LoginResponseDTO newMember = memberService.registerMember(requestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(newMember));
    }

    // 일반 정보 수정
    @PatchMapping("/{userUUID}")
    @PreAuthorize("isAuthenticated() and ((#userPrincipal.uuid.toString().equals(principal.username)) or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<MemberDTO>> updateMember(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                               @RequestBody MemberDTO requestDTO) {
        Member updatedMember = memberService.updateMember(userPrincipal.getUsername(), requestDTO);
        return ResponseEntity.ok(ApiResponse.success(MemberDTO.fromEntity(updatedMember)));
    }

    // 비밀번호 변경
    @PatchMapping("/{userUUID}/password")
    @PreAuthorize("isAuthenticated() and ((#userPrincipal.uuid.toString().equals(principal.username)) or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                            @RequestBody MemberDTO.PasswordRequestDTO request) {
        memberService.updatePassword(userPrincipal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 회원 정보 삭제
    @DeleteMapping("/{userUUID}")
    @PreAuthorize("isAuthenticated() and ((#userPrincipal.uuid.toString().equals(principal.username)) or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                          @RequestBody MemberDTO.PasswordRequestDTO password) {
        memberService.deleteMember(userPrincipal.getUsername(), password);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}

