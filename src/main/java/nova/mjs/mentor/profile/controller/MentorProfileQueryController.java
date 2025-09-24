package nova.mjs.mentor.profile.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.mentor.profile.dto.*;
import nova.mjs.mentor.profile.service.query.MentorProfileQueryService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static nova.mjs.util.response.ApiResponse.success;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/mentors")
@RequiredArgsConstructor
public class MentorProfileQueryController {

    private final MentorProfileQueryService query;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<MentorStatsDTO>> stats() {
        return ok(success(query.getOverviewStatistics()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MentorListItemDTO>>> search(
            MentorSearchConditionDTO cond,
            @PageableDefault(size = 12, sort = "createdAt",
                    direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ok(success(query.findMentorCards(cond, pageable)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<MentorListItemDTO>>> featured(@RequestParam YearMonth month) {
        return ok(success(query.findFeaturedMentorsForMonth(month)));
    }

    /**
     * 상세(헤더+섹션+학력) — 안정적 데이터 (memberUuid 기반)
     */
    @GetMapping("/{memberUuid}")
    public ResponseEntity<ApiResponse<MentorDetailDTO>> detail(@PathVariable UUID memberUuid) {
        return ok(success(query.getMentorDetailByMemberUuid(memberUuid)));
    }

    /**
     * 지표(조회/감사/멘토링) — 자주 변하는 데이터 (memberUuid 기반)
     */
    @GetMapping("/{memberUuid}/metrics")
    public ResponseEntity<ApiResponse<MentorMetricsDTO>> metrics(@PathVariable UUID memberUuid) {
        return ok(success(query.getMentorMetricsByMemberUuid(memberUuid, null)));
    }
}