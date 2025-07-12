package nova.mjs.util.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 공통 비즈니스 예외의 기본 클래스
 * - 모든 커스텀 예외는 이 클래스를 상속받음
 * - ErrorCode를 기반으로 상태코드, 에러메시지 일관성 유지
 */
@Getter
public class BusinessBaseException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 메시지 없이 ErrorCode만 전달하는 기본 생성자
     */
    public BusinessBaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode와 cause(예외 원인)를 함께 전달하는 생성자
     */
    public BusinessBaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * 별도의 커스텀 메시지를 추가로 전달하는 생성자
     */
    public BusinessBaseException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * HTTP 상태 코드 반환
     */
    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }

    /**
     * 에러 코드 키 반환
     */
    public String getError() {
        return errorCode.getError();
    }
}