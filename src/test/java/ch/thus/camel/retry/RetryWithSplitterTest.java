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

import org.apache.camel.CamelExecutionException;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RetryWithSplitterTest extends BaseRetryTest {
    @Test
    public void testSuccessLastMoment() throws InterruptedException {
        processor.nbFails = RetryEndpoint.DEFAULT_MAX_TRIES - 1;
        List<String> bodies =
                new ArrayList<String>(
                        Collections.nCopies(RetryEndpoint.DEFAULT_MAX_TRIES, "1"));
        bodies.add("2");
        bodies.add("3");
        bodies.add("4");
        subEndpoint.expectedBodiesReceived(bodies);
        result.expectedBodiesReceived("#1", "#2", "#3", "#4");
        result.expectedPropertyReceived(TEST_PROP_NAME, TEST_PROP_VALUE);

        template.sendBody("1;2;3;4");
    }

    @Test
    public void testFirstSplitFails() throws InterruptedException {
        try {
            processor.nbFails = RetryEndpoint.DEFAULT_MAX_TRIES;
            List<String> bodies =
                    new ArrayList<String>(
                            Collections.nCopies(RetryEndpoint.DEFAULT_MAX_TRIES, "1"));
            result.expectedMessageCount(0);

            template.sendBody("1;2;3;4");
            fail("Should throw an exception");
        } catch (CamelExecutionException ex) {
            assertNotNull(RetryProducer.getExceptionMatching(ex, RetryExhaustedException.class));
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        processor = new FailProcessor();
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .split(bodyAs(String.class).tokenize(";"))
                            .stopOnException()
                            .to("retry:direct:sub")
                            .to("mock:result")
                        .end();
                from("direct:sub").to("mock:sub").process(processor);
            }
        };
    }

}
