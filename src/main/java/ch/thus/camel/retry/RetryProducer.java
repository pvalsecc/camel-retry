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

import org.apache.camel.Exchange;
import org.apache.camel.StreamCache;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.processor.PipelineHelper;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Camel producer for retry: URLs
 */
public class RetryProducer extends DefaultProducer {
    private final RetryEndpoint endpoint;

    private final static Logger LOGGER = LoggerFactory.getLogger(RetryProducer.class);

    public RetryProducer(RetryEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    public void process(Exchange exchange) throws Exception {
        if (exchange.getIn().getBody() instanceof InputStream) {
            exchange.getIn().setBody(
                    exchange.getContext().getStreamCachingStrategy().cache(exchange));
        }
        Exchange tempExchange = exchange.copy(true);
        try {
            for (int retry = 0; retry < endpoint.getMaxTries(); ++retry) {
                endpoint.getTargetProducer().process(tempExchange);
                if (tempExchange.getException() == null) {
                    onSuccess(tempExchange);
                    return;  //success
                } else if (isExceptionMatching(tempExchange.getException(), FailRetryException.class)) {
                    return;  //forward exception
                } else if (isExceptionMatching(tempExchange.getException(), endpoint.getException())) {
                    //retry
                    LOGGER.debug("{} try {} failed", endpoint.getEndpointUri(), retry + 1);
                    tempExchange = exchange.copy(true);
                    if (exchange.getIn().getBody() instanceof StreamCache) {
                        StreamCache is = (StreamCache) exchange.getIn().getBody();
                        is.reset();
                    }
                } else {
                    return;  //forward exception
                }
            }
            onExhausted(exchange);
            tempExchange.setException(new RetryExhaustedException("Retry exhausted", tempExchange.getException()));
        } finally {
            ExchangeHelper.copyResults(exchange, tempExchange);
        }
    }

    private void onExhausted(Exchange exchange) throws Exception {
        LOGGER.info("{} exhausted maxRetries={}", endpoint.getEndpointUri(),
                endpoint.getMaxTries());
        if (endpoint.getOnExhausted() != null) {
            endpoint.getOnExhausted().process(exchange);
        }
    }

    private void onSuccess(Exchange tempExchange) throws Exception {
        LOGGER.debug("{} success", endpoint.getEndpointUri());
        if (endpoint.getOnSuccess() != null) {
            endpoint.getOnSuccess().process(PipelineHelper.createNextExchange(tempExchange.copy()));
        }
    }

    protected static boolean isExceptionMatching(Throwable ex, Class clazz) {
        if (ex.getCause() != null && isExceptionMatching(ex.getCause(), clazz)) { //deep first
            return true;
        } else {
            return clazz.isInstance(ex);
        }
    }
}
