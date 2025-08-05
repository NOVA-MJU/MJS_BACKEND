package nova.mjs.domain.contact.service;

import nova.mjs.domain.contact.dto.EmailContactDTO;

public interface EmailContactService {
    EmailContactDTO.Response sendContactEmail(EmailContactDTO.Request request);
}