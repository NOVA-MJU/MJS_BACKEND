package nova.mjs.util.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 서버 에러 (S)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "[MJS] 내부 서버 에러가 발생했습니다."),
    API_UNKNOWN_FINISH_REASON(HttpStatus.INTERNAL_SERVER_ERROR, "API_UNKNOWN_FINISH_REASON", "[MJS] 알 수 없는 이유로 응답을 불러올 수 없습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR", "[MJS] 데이터베이스 오류가 발생했습니다."),

    // 마이페이지 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "[MJS] 해당 사용자를 찾을 수 없습니다."),


    // 요청 에러 (R)
    INVALID_PARAM_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_PARAM_REQUEST", "[MJS] 요청된 파람 값이 잘못되었습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "[MJS] 요청된 값이 잘못되었습니다."),
    OFFSET_IS_LESS_THEN_ONE(HttpStatus.BAD_REQUEST, "OFFSET_IS_LESS_THEN_ONE", "[MJS] offset은 1부터 시작합니다."),
    LIMIT_IS_LESS_THEN_ONE(HttpStatus.BAD_REQUEST, "LIMIT_IS_LESS_THEN_ONE", "[MJS] limit은 1부터 시작합니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "[MJS] 잘못된 입력입니다."),
    INVALID_S3_URL(HttpStatus.BAD_REQUEST, "INVALID_S3_URL", "[MJS] 유효하지 않은 S3 Url입니다."),

    // 공지사항 에러 관련 추가 코드
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_NOT_FOUND", "[MJS] 해당 조건의 공지사항을 찾을 수 없습니다."),

    // 자유 게시판 관련 에러(C)
    COMMUNITY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY_NOT_FOUND", "[MJS] 요청한 게시판을 찾을 수 없습니다."),

    WEEKLYMENU_NOT_FOUND(HttpStatus.NOT_FOUND, "WEEKLYMENU_NOT_FOUND", "[MJS] 식단 내용을 찾을 수 없습니다."),

    // 회원 에러(M)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,"MEMBER_NOT_FOUND" ,"[MJS] 회원 정보를 찾을 수 없습니다." ),
    PASSWORD_IS_INVALID(HttpStatus.BAD_REQUEST, "PASSWORD_IS_INVALID", "[MJS] 비밀번호가 잘못되었습니다" ),
    SAME_PASSWORD_NOT_ALLOWED(HttpStatus.BAD_REQUEST,"SAME_PASSWORD_NOT_ALLOWED" , "[MJS] 이전과 동일한 비밀번호로는 변경할 수 없습니다."),
    DUPLICATE_EMAIL_EXCEPTION(HttpStatus.BAD_REQUEST,"DUPLICATE_EMAIL_EXCEPTION", "[MJS] 이미 존재하는 이메일입니다." );




    private final HttpStatus status;
    private final String error;
    private final String message;

    ErrorCode(final HttpStatus status, final String error, final String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
