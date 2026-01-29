package nova.mjs.domain.mentorship.program.service.command;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.mentor.entity.MentorProfile;
import nova.mjs.domain.mentorship.mentor.exception.MentorNotFoundException;
import nova.mjs.domain.mentorship.mentor.repository.MentorProfileRepository;
import nova.mjs.domain.mentorship.program.dto.ProgramAdminDTO;
import nova.mjs.domain.mentorship.program.entity.MentoringProgram;
import nova.mjs.domain.mentorship.program.repository.MentoringProgramRepository;
import nova.mjs.domain.thingo.member.exception.MemberNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * ADMIN 프로그램 Command Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProgramAdminCommandServiceImpl
        implements ProgramAdminCommandService {

    private final MentoringProgramRepository programRepository;
    private final MentorProfileRepository mentorProfileRepository;

    @Override
    public ProgramAdminDTO.CreateResponse createProgram(
            ProgramAdminDTO.CreateRequest request
    ) {
        // 멘토 조회
        List<MentorProfile> mentors =
                mentorProfileRepository.findByMember_EmailIn(request.getMentorEmails());

        // 유효성 검증
        if (mentors.size() != request.getMentorEmails().size()) {
            throw new MentorNotFoundException();
        }

        // 프로그램 생성
        MentoringProgram program = request.toEntity(mentors);
        programRepository.save(program);

        // 등록 응답 (DTO에게 위임)
        return ProgramAdminDTO.CreateResponse.fromEntity(program, mentors);
    }

}
