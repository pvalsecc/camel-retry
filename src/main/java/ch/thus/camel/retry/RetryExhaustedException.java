package ch.thus.camel.retry;

/**
 * Created by patrick on 09.01.16.
 */
public class RetryExhaustedException extends Exception {
    public RetryExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }
}
