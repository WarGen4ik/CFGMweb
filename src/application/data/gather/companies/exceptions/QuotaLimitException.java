package application.data.gather.companies.exceptions;


/**
 * Created by Admin on 30.06.2017.
 */
public class QuotaLimitException extends Exception{
    public QuotaLimitException() {
    }

    public QuotaLimitException(String message) {
        super(message);
    }

    public QuotaLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuotaLimitException(Throwable cause) {
        super(cause);
    }

    public QuotaLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
