package nova.mjs.mentor.profile.service.query;

import nova.mjs.mentor.profile.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.YearMonth;
import java.util.List;

public interface MentorProfileQueryService {
    MentorStatsDTO stats();

    Page<MentorListItemDTO> search(MentorSearchConditionDTO cond, Pageable pageable);

    List<MentorListItemDTO> featured(YearMonth month);

    MentorDetailDTO getDetail(Long id);

    MentorMetricsDTO getMetrics(Long id, Long viewerIdNullable);
}