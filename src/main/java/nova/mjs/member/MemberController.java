package nova.mjs.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.jwt.JwtUtil;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.CustomUserDetailsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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
    @GetMapping("/{userUUID}")
    public ResponseEntity<ApiResponse<MemberDTO>> getMember(@PathVariable UUID userUUID) {
        MemberDTO member = memberService.getMemberByUuid(userUUID);
        return ResponseEntity.ok(ApiResponse.success(member));
    }

    // 회원 정보 생성 (회원 가입)
    @PostMapping
    public ResponseEntity<ApiResponse<MemberDTO>> registerMember(@RequestBody MemberDTO.MemberRequestDTO requestDTO) {
        Member newMember = memberService.registerMember(requestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(MemberDTO.fromEntity(newMember)));
    }

    // 일반 정보 수정
    @PatchMapping("/{userUUID}")
    public ResponseEntity<ApiResponse<MemberDTO>> updateMember(@PathVariable UUID userUUID,
                                                               @RequestBody MemberDTO requestDTO) {
        Member updatedMember = memberService.updateMember(userUUID, requestDTO);
        return ResponseEntity.ok(ApiResponse.success(MemberDTO.fromEntity(updatedMember)));
    }

    // 비밀번호 변경
    @PatchMapping("/{userUUID}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@PathVariable UUID userUUID,
                                                            @RequestBody MemberDTO.PasswordRequestDTO request) {
        memberService.updatePassword(userUUID, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 회원 정보 삭제
    @DeleteMapping("/{userUUID}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable UUID userUUID,
                                                          @RequestBody MemberDTO.PasswordRequestDTO password) {
        memberService.deleteMember(userUUID, password);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}

