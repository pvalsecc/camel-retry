package ch.thus.camel.retry;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.apache.camel.builder.NoErrorHandlerBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.processor.RedeliveryPolicy;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * Created by patrick on 10.01.16.
 */
public class RetryWithErrorHandlerTest extends BaseRetryTest {
    private static final int NB_REDELIVERIES = 1;
    @EndpointInject(uri = "mock:beforeRetry")
    protected MockEndpoint beforeRetry;

    @EndpointInject(uri = "mock:deadLetter")
    protected MockEndpoint deadLetter;

    @Test
    public void testSuccessLastMoment() throws InterruptedException {
        processor.nbFails = RetryEndpoint.DEFAULT_MAX_TRIES - 1;
        List<String> bodies =
                Collections.nCopies(RetryEndpoint.DEFAULT_MAX_TRIES, BODY);
        subEndpoint.expectedBodiesReceived(bodies);
        result.expectedBodiesReceived(RESPONSE);
        result.expectedPropertyReceived(TEST_PROP_NAME, TEST_PROP_VALUE);
        beforeRetry.expectedMessageCount(1);
        deadLetter.expectedMessageCount(0);

        template.sendBody(BODY);
    }

    @Test
    public void testFail() throws InterruptedException {
        processor.nbFails = RetryEndpoint.DEFAULT_MAX_TRIES * (NB_REDELIVERIES + 1);
        List<String> bodies =
                Collections.nCopies(RetryEndpoint.DEFAULT_MAX_TRIES * (NB_REDELIVERIES + 1), BODY);
        subEndpoint.expectedBodiesReceived(bodies);
        result.expectedMessageCount(0);
        beforeRetry.expectedMessageCount(1);
        deadLetter.expectedBodiesReceived(BODY);

        template.sendBody(BODY);
    }

    @Test
    public void testFatal() throws InterruptedException {
        processor.fatal = true;
        processor.nbFails = (NB_REDELIVERIES + 1);
        List<String> bodies =
                Collections.nCopies(NB_REDELIVERIES + 1, BODY);
        subEndpoint.expectedBodiesReceived(bodies);
        result.expectedMessageCount(0);
        beforeRetry.expectedMessageCount(1);
        deadLetter.expectedBodiesReceived(BODY);

        template.sendBody(BODY);
    }

    @Override
    public void checkMocksAreHappy() throws InterruptedException {
        super.checkMocksAreHappy();
        beforeRetry.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        processor = new FailProcessor();
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                DeadLetterChannelBuilder errorHandlerBuilder = new DeadLetterChannelBuilder("mock:deadLetter");
                RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
                redeliveryPolicy.setMaximumRedeliveries(NB_REDELIVERIES);
                errorHandlerBuilder.setRedeliveryPolicy(redeliveryPolicy);
                from("direct:start")
                        .errorHandler(errorHandlerBuilder)
                        .to("mock:beforeRetry")
                        .to("retry:direct:sub").to("mock:result");
                from("direct:sub")
                        .errorHandler(new NoErrorHandlerBuilder())  //TODO: should do that by itself
                        .to("mock:sub")
                        .process(processor);
            }
        };
    }

}
