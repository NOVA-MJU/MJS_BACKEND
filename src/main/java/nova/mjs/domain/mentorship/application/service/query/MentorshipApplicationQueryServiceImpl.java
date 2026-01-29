package nova.mjs.domain.mentorship.application.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.mentorship.application.dto.MentorshipApplicationDTO;
import nova.mjs.domain.mentorship.application.entity.MentorshipApplication;
import nova.mjs.domain.mentorship.application.repository.MentorshipApplicationRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.service.query.MemberQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentorshipApplicationQueryServiceImpl implements MentorshipApplicationQueryService {

    private final MentorshipApplicationRepository applicationRepository;
    private final MemberQueryService memberQueryService;

    @Override
    public Page<MentorshipApplicationDTO.SummaryResponse> getMyApplications(
            String emailId,
            Pageable pageable
    ) {
        Member me = memberQueryService.getMemberByEmail(emailId);

        return applicationRepository.findByApplicant(me, pageable)
                .map(MentorshipApplicationDTO.SummaryResponse::fromEntity);
    }

    @Override
    public Page<MentorshipApplicationDTO.SummaryResponse> getApplicationsForMentor(
            String emailId,
            Pageable pageable
    ) {
        Member me = memberQueryService.getMemberByEmail(emailId);

        return applicationRepository.findByMentor(me, pageable)
                .map(MentorshipApplicationDTO.SummaryResponse::fromEntity);
    }

    @Override
    public MentorshipApplicationDTO.DetailResponse getApplicationDetail(
            String emailId,
            UUID applicationUuid
    ) {
        Member me = memberQueryService.getMemberByEmail(emailId);

        MentorshipApplication app = applicationRepository.findByUuid(applicationUuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청입니다."));

        // 본인(신청자/멘토)만 조회 가능
        boolean isApplicant = app.getApplicant().getId().equals(me.getId());
        boolean isMentor = app.getMentor().getId().equals(me.getId());

        if (!isApplicant && !isMentor) {
            throw new IllegalStateException("신청 상세 조회 권한이 없습니다.");
        }

        return MentorshipApplicationDTO.DetailResponse.fromEntity(app);
    }
}
