package nova.mjs.domain.mentorship.program.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.program.dto.ProgramAdminDTO;
import nova.mjs.domain.mentorship.program.service.command.ProgramAdminCommandService;
import nova.mjs.domain.mentorship.program.service.query.ProgramAdminQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/programs")
@PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
public class ProgramAdminController {

    private final ProgramAdminCommandService commandService;
    private final ProgramAdminQueryService queryService;

    // 프로그램 생성
    @PostMapping
    public ProgramAdminDTO.CreateResponse create(@RequestBody ProgramAdminDTO.CreateRequest request) {
        return commandService.createProgram(request);
    }

    // 프로그램 리스트 페이지네이션
    @GetMapping
    public Page<ProgramAdminDTO.SummaryResponse> list(Pageable pageable) {
        return queryService.getPrograms(pageable);
    }


    // 프로그램 상세 조회
    @GetMapping("/{programId}")
    public ProgramAdminDTO.DetailResponse detail(
            @PathVariable UUID programId
    ) {
        return queryService.getProgramDetail(programId);
    }
}
