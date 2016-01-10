package ch.thus.camel.retry;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class RetryConfiguredTest extends BaseRetryTest {
    private static final int MAX_RETRIES = 3;
    @EndpointInject(uri = "mock:onSuccess")
    protected MockEndpoint onSuccess;

    @EndpointInject(uri = "mock:onExhausted")
    protected MockEndpoint onExhausted;

    @Test
    public void testSuccessRightAway() throws InterruptedException {
        subEndpoint.expectedBodiesReceived(BODY);
        result.expectedBodiesReceived(RESPONSE);
        result.expectedPropertyReceived(TEST_PROP_NAME, TEST_PROP_VALUE);
        onSuccess.expectedBodiesReceived(RESPONSE);
        onSuccess.expectedPropertyReceived(TEST_PROP_NAME, TEST_PROP_VALUE);
        onExhausted.expectedMessageCount(0);

        template.sendBody(BODY);
    }

    @Test
    public void testSuccessLastMoment() throws InterruptedException {
        processor.nbFails = MAX_RETRIES - 1;
        List<String> bodies =
                Collections.nCopies(MAX_RETRIES, BODY);
        subEndpoint.expectedBodiesReceived(bodies);
        result.expectedBodiesReceived(RESPONSE);
        result.expectedPropertyReceived(TEST_PROP_NAME, TEST_PROP_VALUE);
        onSuccess.expectedBodiesReceived(RESPONSE);
        onSuccess.expectedPropertyReceived(TEST_PROP_NAME, TEST_PROP_VALUE);
        onExhausted.expectedMessageCount(0);

        template.sendBody(BODY);
    }

    @Test
    public void testFail() throws InterruptedException {
        try {
            processor.nbFails = MAX_RETRIES;
            List<String> bodies =
                    Collections.nCopies(MAX_RETRIES, BODY);
            subEndpoint.expectedBodiesReceived(bodies);
            result.expectedMessageCount(0);
            onSuccess.expectedMessageCount(0);
            onExhausted.expectedBodiesReceived(BODY);

            template.sendBody(BODY);
            fail("Expected a failure");
        } catch (CamelExecutionException camelException) {
            assertEquals(RetryExhaustedException.class, camelException.getCause().getClass());
        }
    }

    @Test
    public void testFatal() throws InterruptedException {
        try {
            processor.fatal = true;
            processor.nbFails = MAX_RETRIES;
            subEndpoint.expectedBodiesReceived(BODY);
            result.expectedMessageCount(0);
            onSuccess.expectedMessageCount(0);
            onExhausted.expectedMessageCount(0);

            template.sendBody(BODY);

            fail("Expected a failure");
        } catch (CamelExecutionException camelException) {
            assertEquals(FailRetryException.class, camelException.getCause().getClass());
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        processor = new FailProcessor();
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").
                        to(String.format("retry:direct:sub?maxTries=%d&onSuccess=mock:onSuccess&onExhausted=mock:onExhausted", MAX_RETRIES)).
                        to("mock:result");
                from("direct:sub").to("mock:sub").process(processor);
            }
        };
    }

    public void checkMocksAreHappy() throws InterruptedException {
        super.checkMocksAreHappy();
        onSuccess.assertIsSatisfied();
        onExhausted.assertIsSatisfied();
    }
}
