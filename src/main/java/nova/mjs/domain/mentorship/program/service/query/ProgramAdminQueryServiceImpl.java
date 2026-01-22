package nova.mjs.domain.mentorship.program.service.query;

import lombok.RequiredArgsConstructor;
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
public class ProgramAdminQueryServiceImpl
        implements ProgramAdminQueryService {

    private final MentoringProgramRepository programRepository;

    @Override
    public Page<ProgramAdminDTO.SummaryResponse> getPrograms(Pageable pageable) {
        return programRepository.findAll(pageable)
                .map(p -> ProgramAdminDTO.SummaryResponse.builder()
                        .programUuid(p.getUuid())
                        .title(p.getTitle())
                        .startDate(p.getStartDate())
                        .endDate(p.getEndDate())
                        .mentorCount(p.getMentors().size())
                        .build());
    }

    @Override
    public ProgramAdminDTO.DetailResponse getProgramDetail(UUID uuid) {

        MentoringProgram program = programRepository.findDetailByUuid(uuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 프로그램입니다.")
                );

        return ProgramAdminDTO.DetailResponse.builder()
                .programUuid(program.getUuid())
                .title(program.getTitle())
                .description(program.getDescription())
                .startDate(program.getStartDate())
                .endDate(program.getEndDate())
                .capacity(program.getCapacity())
                .targetAudience(program.getTargetAudience())
                .location(program.getLocation())
                .contact(program.getContact())
                .preparation(program.getPreparation())
                .mentorEmails(
                        program.getMentors().stream()
                                .map(mp -> mp.getMember().getEmail())
                                .toList()
                )
                .build();
    }
}
