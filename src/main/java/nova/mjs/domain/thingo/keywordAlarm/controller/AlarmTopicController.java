package nova.mjs.domain.thingo.keywordAlarm.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.keywordAlarm.dto.AlarmTopicDTO;
import nova.mjs.domain.thingo.keywordAlarm.service.AlarmTopicService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 게시글이 아니라 구독 가능한 표준 Topic을 반환한다. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/alarm-topics")
public class AlarmTopicController {

    private final AlarmTopicService alarmTopicService;

    @GetMapping("/autocomplete")
    public ApiResponse<AlarmTopicDTO.AutocompleteResponse> autocomplete(
            @RequestParam("query") String query,
            @RequestParam(name = "limit", defaultValue = "8") int limit) {
        return ApiResponse.success(alarmTopicService.autocomplete(query, limit));
    }

    /** 추천 칩 표시와 등록에 필요한 짧은 문구 + 표준 Topic ID를 함께 반환한다. */
    @GetMapping("/recommended")
    public ApiResponse<AlarmTopicDTO.RecommendedResponse> recommended() {
        return ApiResponse.success(alarmTopicService.recommended());
    }
}
