package nova.mjs.util.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessBaseException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessBaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessBaseException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }

    public String getError() {
        return errorCode.getError();
    }
}