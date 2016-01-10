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

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 * Camel component for retry: URLs
 */
public class RetryComponent extends DefaultComponent {
    @Override
    protected Endpoint createEndpoint(java.lang.String uri, java.lang.String remaining, java.util.Map<java.lang.String, java.lang.Object> parameters) throws Exception {
        Endpoint endpoint = new RetryEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
