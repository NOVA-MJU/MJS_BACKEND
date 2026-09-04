package nova.mjs.domain.thingo.keywordAlarm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.keywordAlarm.dto.ManualAlarmDTO;
import nova.mjs.domain.thingo.keywordAlarm.service.ManualKeywordAlarmService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 키워드 알림 수동 발송 컨트롤러.
 *
 * - POST /api/v1/admin/keyword-alarms/manual-send
 *   특정 회원(email)에게 키워드에 매칭되는 과거 콘텐츠 1건을 FCM 푸시로 발송한다.
 *   (자동 매칭 파이프라인과 별개인 운영/데모용 수동 발송)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/keyword-alarms")
public class AdminKeywordAlarmController {

    private final ManualKeywordAlarmService manualKeywordAlarmService;

    @PostMapping("/manual-send")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ApiResponse<ManualAlarmDTO.Response> manualSend(
            @Valid @RequestBody ManualAlarmDTO.Request request) {
        log.info("[수동알림] 요청 - email={}, keyword={}", request.getEmail(), request.getKeyword());
        ManualAlarmDTO.Response response = manualKeywordAlarmService.send(
                request.getEmail(), request.getKeyword(), request.getSearchIndexId());
        return ApiResponse.success(response);
    }
}
