package nova.mjs.util.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {
    @Schema(description = "HTTP 상태 코드")
    private final int status;

    @Schema(description = "오류 코드")
    private final String error;

    @Schema(description = "오류 메시지")
    private final String message;

    private ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.error = errorCode.name();
        this.message = errorCode.getMessage();
    }

    private ErrorResponse(ErrorCode errorCode, String message) {
        this.status = errorCode.getStatus().value();
        this.error = errorCode.name();
        this.message = message;
    }

    private ErrorResponse(HttpStatus status, String error, String message) {
        this.status = status.value();
        this.error = error;
        this.message = message;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode, message);
    }

    public static ErrorResponse of(HttpStatus httpStatus, String error, String message) {
        return new ErrorResponse(httpStatus, error, message);
    }
}