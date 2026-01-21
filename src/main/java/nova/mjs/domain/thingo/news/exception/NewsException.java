package nova.mjs.domain.thingo.news.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class NewsException extends BusinessBaseException {

    public NewsException(ErrorCode errorCode) {super(errorCode);}

    public NewsException(String message, ErrorCode errorCode) {super(message, errorCode);}
}
