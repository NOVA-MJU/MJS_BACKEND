package nova.mjs.mentor.profile.service.query;

import nova.mjs.mentor.profile.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface MentorProfileQueryService {
    /**
     * 목록 상단 개요 통계 카드
     */
    MentorStatsDTO getOverviewStatistics();

    /**
     * 멘토 카드 목록 페이지 조회
     */
    Page<MentorListItemDTO> findMentorCards(MentorSearchConditionDTO condition, Pageable pageable);

    /**
     * 이달의 추천 멘토들
     */
    List<MentorListItemDTO> findFeaturedMentorsForMonth(YearMonth month);

    /**
     * 상세 페이지 본문(헤더+섹션+학력)
     */
    MentorDetailDTO getMentorDetailByMemberUuid(UUID memberUuid);

    /**
     * 상세 페이지 지표(조회/감사/멘토링)
     */
    MentorMetricsDTO getMentorMetricsByMemberUuid(UUID memberUuid, Long viewerIdNullable);
}