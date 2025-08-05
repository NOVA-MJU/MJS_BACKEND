package nova.mjs.domain.contact;

import nova.mjs.domain.contact.dto.EmailContactDTO;

public interface EmailContactService {
    EmailContactDTO.Response sendContactEmail(EmailContactDTO.Request request);
}