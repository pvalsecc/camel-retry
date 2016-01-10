package ch.thus.camel.retry;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class RetryDefaultTest extends BaseRetryTest {
    @Test
    public void testSuccessRightAway() throws InterruptedException {
        subEndpoint.expectedBodiesReceived(BODY);
        result.expectedBodiesReceived(RESPONSE);
        result.expectedPropertyReceived(TEST_PROP_NAME, TEST_PROP_VALUE);

        template.sendBody(BODY);
    }

    @Test
    public void testSuccessLastMoment() throws InterruptedException {
        processor.nbFails = RetryEndpoint.DEFAULT_MAX_TRIES - 1;
        List<String> bodies =
                Collections.nCopies(RetryEndpoint.DEFAULT_MAX_TRIES, BODY);
        subEndpoint.expectedBodiesReceived(bodies);
        result.expectedBodiesReceived(RESPONSE);
        result.expectedPropertyReceived(TEST_PROP_NAME, TEST_PROP_VALUE);

        template.sendBody(BODY);
    }

    @Test
    public void testFail() throws InterruptedException {
        try {
            processor.nbFails = RetryEndpoint.DEFAULT_MAX_TRIES;
            List<String> bodies =
                    Collections.nCopies(RetryEndpoint.DEFAULT_MAX_TRIES, BODY);
            subEndpoint.expectedBodiesReceived(bodies);
            result.expectedMessageCount(0);

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
            processor.nbFails = RetryEndpoint.DEFAULT_MAX_TRIES;
            subEndpoint.expectedBodiesReceived(BODY);
            result.expectedMessageCount(0);

            template.sendBody(BODY);

            fail("Expected a failure");
        } catch (CamelExecutionException camelException) {
            assertEquals(FailRetryException.class, camelException.getCause().getClass());
        }
    }

    @Test
    public void testStreamBodiesAreSupported() throws Exception {
        processor.nbFails = 1;
        List<String> bodies =
                Collections.nCopies(2, BODY);
        subEndpoint.expectedBodiesReceived(bodies);
        result.expectedBodiesReceived(RESPONSE);

        InputStream body = new ByteArrayInputStream(BODY.getBytes("utf-8"));
        template.sendBody(body);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        processor = new FailProcessor();
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("retry:direct:sub").to("mock:result");
                from("direct:sub").to("mock:sub").process(processor);
            }
        };
    }
}
