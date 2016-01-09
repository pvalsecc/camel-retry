package ch.thus.camel.retry;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class RetryProducerTest {

    @Test
    public void testIsExceptionMatching() throws Exception {
        IllegalAccessException illegalAccess = new IllegalAccessException();
        assertTrue(RetryProducer.isExceptionMatching(illegalAccess, IllegalAccessException.class));
        assertFalse(RetryProducer.isExceptionMatching(illegalAccess, IOException.class));
        assertTrue(RetryProducer.isExceptionMatching(illegalAccess, ReflectiveOperationException.class));


        CamelExecutionException camel = new CamelExecutionException("boom", new DefaultExchange((CamelContext) null), illegalAccess);
        assertTrue(RetryProducer.isExceptionMatching(camel, IllegalAccessException.class));
        assertFalse(RetryProducer.isExceptionMatching(camel, IOException.class));
        assertTrue(RetryProducer.isExceptionMatching(camel, ReflectiveOperationException.class));
    }
}