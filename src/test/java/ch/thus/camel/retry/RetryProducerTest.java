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

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RetryProducerTest {

    @Test
    public void testIsExceptionMatching() throws Exception {
        IllegalAccessException illegalAccess = new IllegalAccessException();
        assertEquals(illegalAccess, RetryProducer.getExceptionMatching(illegalAccess, IllegalAccessException.class));
        assertNull(RetryProducer.getExceptionMatching(illegalAccess, IOException.class));
        assertEquals(illegalAccess, RetryProducer.getExceptionMatching(illegalAccess, ReflectiveOperationException.class));


        CamelExecutionException camel = new CamelExecutionException("boom", new DefaultExchange((CamelContext) null), illegalAccess);
        assertEquals(illegalAccess, RetryProducer.getExceptionMatching(camel, IllegalAccessException.class));
        assertNull(RetryProducer.getExceptionMatching(camel, IOException.class));
        assertEquals(illegalAccess, RetryProducer.getExceptionMatching(camel, ReflectiveOperationException.class));
    }
}