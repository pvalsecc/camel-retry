/*
 * Copyright 2016 Patrick Valsecchi
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.thus.camel.retry;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            String body = exchange.getIn().getBody(String.class);
            if (body.matches("\\d+")) {
                exchange.getOut().setBody("#" + body);
            } else {
                exchange.getOut().setBody(RESPONSE);
            }
            exchange.setProperty(TEST_PROP_NAME, TEST_PROP_VALUE);
        }
    }

    @After
    public void checkMocksAreHappy() throws InterruptedException {
        subEndpoint.assertIsSatisfied();
        result.assertIsSatisfied();
    }
}
