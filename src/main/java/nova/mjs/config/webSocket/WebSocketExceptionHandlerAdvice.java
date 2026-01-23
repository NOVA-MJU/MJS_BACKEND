package nova.mjs.config.webSocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.util.response.ApiResponse;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class WebSocketExceptionHandlerAdvice {

    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * BusinessBaseException -> ErrorCode 기반으로 내려주기
     */
    @MessageExceptionHandler(BusinessBaseException.class)
    public void handleBusiness(BusinessBaseException e, SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        String userKey = (principal != null) ? principal.getName() : "anonymous";

        ErrorCode ec = e.getErrorCode();

        // ApiResponse.fail이 없으니, ApiResponse 생성자로 직접 만들기
        ApiResponse<WsErrorPayload> response = new ApiResponse<>(
                String.valueOf(ec.getStatus().value()),      // "401" / "400" / "500" 등
                new WsErrorPayload(ec.getError(), ec.getMessage()),
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/sub/chat/error/" + userKey, response);
    }

    /**
     * 그 외 예상 못 한 예외도 안전하게 처리
     */
    @MessageExceptionHandler(Exception.class)
    public void handleEtc(Exception e, SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        String userKey = (principal != null) ? principal.getName() : "anonymous";

        log.error("[WS] unexpected error", e);

        ApiResponse<WsErrorPayload> response = new ApiResponse<>(
                "500",
                new WsErrorPayload("WEBSOCKET_ERROR", e.getMessage()),
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/sub/chat/error/" + userKey, response);
    }

    @Getter
    @AllArgsConstructor
    public static class WsErrorPayload {
        private String error;    // ErrorCode.error 또는 "WEBSOCKET_ERROR"
        private String message;  // ErrorCode.message 또는 예외 메시지
    }
}
