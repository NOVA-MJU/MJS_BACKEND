package nova.mjs.domain.mentorship.application.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.application.dto.MentorshipApplicationDTO;
import nova.mjs.domain.mentorship.application.service.MentorshipApplicationCommandService;
import nova.mjs.domain.mentorship.application.service.query.MentorshipApplicationQueryService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mentorship/applications")
public class MentorshipApplicationController {

    private final MentorshipApplicationQueryService applicationQueryService;
    private final MentorshipApplicationCommandService applicationCommandService;

    /* ===============================
       멘토링 신청 생성
       =============================== */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorshipApplicationDTO.CreateResponse>> createApplication(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody MentorshipApplicationDTO.CreateRequest request
    ) {
        String emailId = (userPrincipal != null) ? userPrincipal.getUsername() : null;

        MentorshipApplicationDTO.CreateResponse response =
                applicationCommandService.createApplication(request, emailId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /* ===============================
       멘토링 신청 수락
       =============================== */
    @PostMapping("/{applicationUuid}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> acceptApplication(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String applicationUuid
    ) {
        // 권한 검증(멘토 여부)은 Service에서 처리
        applicationCommandService.acceptApplication(applicationUuid);

        return ResponseEntity.ok(ApiResponse.success());
    }

    /* ===============================
       멘토링 신청 거절
       =============================== */
    @PostMapping("/{applicationUuid}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> rejectApplication(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String applicationUuid
    ) {
        // 권한 검증(멘토 여부)은 Service에서 처리
        applicationCommandService.rejectApplication(applicationUuid);

        return ResponseEntity.ok(ApiResponse.success());
    }
    /**
     * 내가 신청자로서 만든 신청 목록
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<MentorshipApplicationDTO.SummaryResponse>>> getMyApplications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable
    ) {
        String emailId = (userPrincipal != null) ? userPrincipal.getUsername() : null;

        Page<MentorshipApplicationDTO.SummaryResponse> result =
                applicationQueryService.getMyApplications(emailId, pageable);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 내가 멘토로서 받은 신청 목록
     */
    @GetMapping("/mentor")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<MentorshipApplicationDTO.SummaryResponse>>> getApplicationsForMentor(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable
    ) {
        String emailId = (userPrincipal != null) ? userPrincipal.getUsername() : null;

        Page<MentorshipApplicationDTO.SummaryResponse> result =
                applicationQueryService.getApplicationsForMentor(emailId, pageable);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 신청 상세 (신청자/멘토 본인만)
     */
    @GetMapping("/{applicationUuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorshipApplicationDTO.DetailResponse>> getApplicationDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID applicationUuid
    ) {
        String emailId = (userPrincipal != null) ? userPrincipal.getUsername() : null;

        MentorshipApplicationDTO.DetailResponse result =
                applicationQueryService.getApplicationDetail(emailId, applicationUuid);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
