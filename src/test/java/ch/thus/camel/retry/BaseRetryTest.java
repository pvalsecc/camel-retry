package ch.thus.camel.retry;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by patrick on 09.01.16.
 */
public abstract class BaseRetryTest extends CamelTestSupport {
    @EndpointInject(uri = "mock:sub")
    protected MockEndpoint subEndpoint;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    protected FailProcessor processor;

    protected static final String BODY = "Hello";
    protected static final String RESPONSE = "world";
    public static final String TEST_PROP_NAME = "TEST_PROP";
    protected static final int TEST_PROP_VALUE = 36;

    private static Logger LOGGER = LoggerFactory.getLogger(RetryConfiguredTest.class);

    protected static class FailProcessor implements Processor {
        protected int nbFails = 0;
        public boolean fatal;

        @Override
        public void process(Exchange exchange) throws Exception {
            if (nbFails > 0) {
                --nbFails;
                if (fatal) {
                    LOGGER.info("Making the retry fail right away");
                    throw new FailRetryException("stop!");
                } else {
                    LOGGER.info("Making the processor fail");
                    throw new Exception("test");
                }
            }
            exchange.getOut().setBody(RESPONSE);
            exchange.setProperty(TEST_PROP_NAME, TEST_PROP_VALUE);
        }
    }

    @After
    public void checkMocksAreHappy() throws InterruptedException {
        subEndpoint.assertIsSatisfied();
        result.assertIsSatisfied();
    }
}
