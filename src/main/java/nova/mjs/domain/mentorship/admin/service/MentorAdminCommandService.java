package nova.mjs.domain.mentorship.admin.service;

import nova.mjs.domain.mentorship.admin.dto.MentorAdminDTO;

public interface MentorAdminCommandService {
    MentorAdminDTO.Response registerMentor(MentorAdminDTO.Request request);
}