package ch.thus.camel.retry;

import org.apache.camel.*;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;

/**
 * Created by patrick on 09.01.16.
 */
@UriEndpoint(scheme = "retry", syntax = "retry:targetComponent",
        title = "Retry delivery", producerOnly = true)
public class RetryEndpoint extends DefaultEndpoint {
    public static final int DEFAULT_MAX_RETRIES = 5;

    @UriParam
    private int maxRetries = DEFAULT_MAX_RETRIES;

    @UriParam
    private Class<? extends Throwable> exception = Exception.class;

    @UriParam
    private Producer onSuccess;

    @UriParam
    private Producer onExhausted;

    private final Producer targetProducer;

    public RetryEndpoint(String uri, RetryComponent retryComponent) throws Exception {
        super(uri, retryComponent);
        final String targetUri = uri.replaceAll("^retry:/?/?", "").replaceFirst("\\?.*", "");
        final Endpoint targetEndpoint = getCamelContext().getEndpoint(targetUri);
        targetProducer = targetEndpoint.createProducer();
    }

    @Override
    public Producer createProducer() throws Exception {
        return new RetryProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new RuntimeCamelException("Cannot consume from a RetryEndpoint");
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Class getException() {
        return exception;
    }

    @SuppressWarnings("unchecked")
    public void setException(String exception) throws ClassNotFoundException {
        this.exception = (Class<? extends Throwable>)
                getClass().getClassLoader().loadClass(exception);
    }

    public Producer getTargetProducer() {
        return targetProducer;
    }

    public Producer getOnSuccess() {
        return onSuccess;
    }

    public void setOnSuccess(String onSuccess) throws Exception {
        final Endpoint endpoint = getCamelContext().getEndpoint(onSuccess);
        this.onSuccess = endpoint.createProducer();
    }

    public Producer getOnExhausted() {
        return onExhausted;
    }

    public void setOnExhausted(String onExhausted) throws Exception {
        final Endpoint endpoint = getCamelContext().getEndpoint(onExhausted);
        this.onExhausted = endpoint.createProducer();
    }
}
