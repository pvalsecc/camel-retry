package ch.thus.camel.retry;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 * Created by patrick on 09.01.16.
 */
public class RetryComponent extends DefaultComponent {
    @Override
    protected Endpoint createEndpoint(java.lang.String uri, java.lang.String remaining, java.util.Map<java.lang.String, java.lang.Object> parameters) throws Exception {
        Endpoint endpoint = new RetryEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
