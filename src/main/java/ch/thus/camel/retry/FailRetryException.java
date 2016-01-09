package ch.thus.camel.retry;

/**
 * Created by patrick on 09.01.16.
 */
public class FailRetryException extends Exception {
    public FailRetryException(String message) {
        super(message);
    }
}
