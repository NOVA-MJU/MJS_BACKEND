package nova.mjs.domain.mentorship.admin.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.admin.dto.MentorAdminDTO;
import nova.mjs.domain.mentorship.admin.service.MentorAdminCommandService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/mentors")
public class MentorAdminController {

    private final MentorAdminCommandService mentorAdminCommandService;

    /**
     * 관리자 멘토 등록
     */
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    @PostMapping
    public MentorAdminDTO.Response registerMentor(
            @RequestBody MentorAdminDTO.Request request
    ) {
        return mentorAdminCommandService.registerMentor(request);
    }
}
