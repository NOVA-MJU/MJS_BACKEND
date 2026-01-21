package nova.mjs.domain.thingo.contact.service;

import nova.mjs.domain.thingo.contact.dto.EmailContactDTO;

public interface EmailContactService {
    EmailContactDTO.Response sendContactEmail(EmailContactDTO.Request request);
}