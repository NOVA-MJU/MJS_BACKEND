package nova.mjs.domain.mentorship.program.service.command;

import nova.mjs.domain.mentorship.program.dto.ProgramAdminDTO;

import java.util.UUID;

public interface ProgramAdminCommandService {

    /**
     * ADMIN 프로그램 등록
     *
     * @return 생성된 프로그램 uuid
     */
    ProgramAdminDTO.CreateResponse createProgram(ProgramAdminDTO.CreateRequest request);
}
