package nova.mjs.domain.mentorship.program.service.query;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.application.entity.MentorshipApplication;
import nova.mjs.domain.mentorship.application.repository.MentorshipApplicationRepository;
import nova.mjs.domain.mentorship.program.dto.ProgramAdminDTO;
import nova.mjs.domain.mentorship.program.entity.MentoringProgram;
import nova.mjs.domain.mentorship.program.repository.MentoringProgramRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ADMIN 프로그램 Query Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramQueryServiceImpl
        implements ProgramQueryService {

    private final MentoringProgramRepository programRepository;
    private final MentorshipApplicationRepository applicationRepository;

    @Override
    public Page<ProgramAdminDTO.SummaryResponse> getPrograms(Pageable pageable) {
        return programRepository.findAll(pageable)
                .map(ProgramAdminDTO.SummaryResponse::fromEntity);
    }

    @Override
    public ProgramAdminDTO.DetailResponse getProgramDetail(UUID uuid) {
        MentoringProgram program = programRepository.findDetailByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로그램입니다."));

        long currentApplicants =
                applicationRepository.countByProgramUuidAndStatus(
                        uuid,
                        MentorshipApplication.ApplicationStatus.SUBMITTED
                );
        return ProgramAdminDTO.DetailResponse.fromEntity(program, currentApplicants);
    }
}
