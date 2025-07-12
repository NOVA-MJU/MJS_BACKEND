package nova.mjs.util.s3.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class InvalidS3UrlException extends BusinessBaseException {

    public InvalidS3UrlException(ErrorCode errorCode) {
        super(errorCode);
    }
}