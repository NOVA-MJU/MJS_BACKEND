package nova.mjs.domain.mentorship.application.service.query;

import nova.mjs.domain.mentorship.application.dto.MentorshipApplicationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MentorshipApplicationQueryService {

    /**
     * 내가 신청자로서 만든 신청 목록
     */
    Page<MentorshipApplicationDTO.SummaryResponse> getMyApplications(
            String emailId,
            Pageable pageable
    );

    /**
     * 내가 멘토로서 받은 신청 목록
     */
    Page<MentorshipApplicationDTO.SummaryResponse> getApplicationsForMentor(
            String emailId,
            Pageable pageable
    );

    /**
     * 신청 상세 조회 (신청자/멘토 본인만 접근 허용)
     */
    MentorshipApplicationDTO.DetailResponse getApplicationDetail(
            String emailId,
            UUID applicationUuid
    );
}
