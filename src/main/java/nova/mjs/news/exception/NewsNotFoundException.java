package nova.mjs.news.exception;

import nova.mjs.util.exception.ErrorCode;

public class NewsNotFoundException extends NewsException {

    public NewsNotFoundException() {super(ErrorCode.NEWS_NOT_FOUND);}

    public NewsNotFoundException(ErrorCode errorCode) {super(errorCode);}
    public NewsNotFoundException(String message, ErrorCode errorCode) {super(message, errorCode);}

}
