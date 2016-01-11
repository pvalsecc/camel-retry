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

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;

/**
 * Camel endpoint for the retry: component.
 */
@UriEndpoint(scheme = "retry", syntax = "retry:targetComponent",
        title = "Retry delivery", producerOnly = true)
public class RetryEndpoint extends DefaultEndpoint {
    public static final int DEFAULT_MAX_TRIES = 5;

    @UriParam
    private int maxTries = DEFAULT_MAX_TRIES;

    @UriParam
    private Class<? extends Throwable> exception = Exception.class;

    @UriParam
    private Producer onSuccess;

    @UriParam
    private Producer onExhausted;

    @UriParam
    private int retryDelay = 0;

    private final Producer targetProducer;

    public RetryEndpoint(String uri, RetryComponent retryComponent) throws Exception {
        super(uri, retryComponent);
        final String targetUri = uri.replaceAll("^retry:/?/?", "").replaceFirst("\\?.*", "");
        final Endpoint targetEndpoint = getCamelContext().getEndpoint(targetUri);
        targetProducer = targetEndpoint.createProducer();
    }

    @Override
    public Producer createProducer() throws Exception {
        return new RetryProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new RuntimeCamelException("Cannot consume from a RetryEndpoint");
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public int getMaxTries() {
        return maxTries;
    }

    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }

    public Class getException() {
        return exception;
    }

    @SuppressWarnings("unchecked")
    public void setException(String exception) throws ClassNotFoundException {
        this.exception = (Class<? extends Throwable>)
                getClass().getClassLoader().loadClass(exception);
    }

    public Producer getTargetProducer() {
        return targetProducer;
    }

    public Producer getOnSuccess() {
        return onSuccess;
    }

    public void setOnSuccess(String onSuccess) throws Exception {
        final Endpoint endpoint = getCamelContext().getEndpoint(onSuccess);
        this.onSuccess = endpoint.createProducer();
    }

    public Producer getOnExhausted() {
        return onExhausted;
    }

    public void setOnExhausted(String onExhausted) throws Exception {
        final Endpoint endpoint = getCamelContext().getEndpoint(onExhausted);
        this.onExhausted = endpoint.createProducer();
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
    }
}
