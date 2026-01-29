package nova.mjs.domain.mentorship.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.mentorship.application.dto.MentorshipApplicationDTO;
import nova.mjs.domain.mentorship.application.entity.MentorshipApplication;
import nova.mjs.domain.mentorship.application.repository.MentorshipApplicationRepository;
import nova.mjs.domain.thingo.member.DTO.MemberDTO;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.service.query.MemberQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MentorshipApplicationCommandServiceImpl
        implements MentorshipApplicationCommandService {

    private final MentorshipApplicationRepository applicationRepository;
    private final MemberQueryService memberQueryService;

    @Override
    public MentorshipApplicationDTO.CreateResponse createApplication(
            MentorshipApplicationDTO.CreateRequest request,
            String emailId
    ) {
        log.info("[멘토링 신청] 신청자 이메일: {}", emailId);

        // 신청자 조회 (로그인 유저)
        Member applicant = memberQueryService.getMemberByEmail(emailId);

        // 멘토 조회 (UUID 기반)
        Member mentor = memberQueryService.getMemberEntityByUuid(request.getMentorUuid());

        // Application 생성
        MentorshipApplication application =
                request.toEntity(applicant, mentor);

        applicationRepository.save(application);

        return MentorshipApplicationDTO.CreateResponse.fromEntity(application);
    }

    @Override
    public void acceptApplication(String applicationUuid) {
        MentorshipApplication application = getApplication(applicationUuid);

        application.accept();
        // ChatRoom 생성은 여기서 or 별도 Service로 분리 가능
    }

    @Override
    public void rejectApplication(String applicationUuid) {
        MentorshipApplication application = getApplication(applicationUuid);
        application.reject();
    }

    /* ===============================
       내부 헬퍼
       =============================== */

    private MentorshipApplication getApplication(String uuid) {
        return applicationRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));
    }
}
