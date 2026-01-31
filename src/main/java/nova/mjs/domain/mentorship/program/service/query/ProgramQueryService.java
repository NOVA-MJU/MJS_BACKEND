package nova.mjs.domain.mentorship.program.service.query;

import nova.mjs.domain.mentorship.program.dto.ProgramAdminDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProgramQueryService {

    /**
     * 프로그램 목록 조회 (Pagination)
     */
    Page<ProgramAdminDTO.SummaryResponse> getPrograms(Pageable pageable);

    /**
     * 프로그램 상세 조회 (uuid 기준)
     */
    ProgramAdminDTO.DetailResponse getProgramDetail(UUID programUuid);
}
