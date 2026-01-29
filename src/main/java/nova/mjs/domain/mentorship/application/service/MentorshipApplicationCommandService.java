package nova.mjs.domain.mentorship.application.service;

import nova.mjs.domain.mentorship.application.dto.MentorshipApplicationDTO;

public interface MentorshipApplicationCommandService {

    MentorshipApplicationDTO.CreateResponse createApplication(
            MentorshipApplicationDTO.CreateRequest request,
            String emailId
    );

    void acceptApplication(String applicationUuid);

    void rejectApplication(String applicationUuid);
}

