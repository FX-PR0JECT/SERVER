package FXPROJECT.CHECKPASS.domain.common.exception;

public class NoCourseHistory extends RuntimeException{

    public NoCourseHistory() {
    }

    public NoCourseHistory(String message) {
        super(message);
    }

    public NoCourseHistory(String message, Throwable cause) {
        super(message, cause);
    }

    public NoCourseHistory(Throwable cause) {
        super(cause);
    }

    public NoCourseHistory(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
