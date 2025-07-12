package nova.mjs.util.s3.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class FileUploadFailedException extends BusinessBaseException {

    /**
     * 파일 업로드 실패 예외
     *
     * @param errorCode 에러 코드
     * @param cause     원인 예외
     */
    public FileUploadFailedException(ErrorCode errorCode, Throwable  cause) {
        super(errorCode, cause);
    }
}