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

/**
 * If this exception is raised within a retry: route, the retry will stop with a
 * failure event is all the retries are not exhausted.
 */
public class FailRetryException extends Exception {
    public FailRetryException(String message) {
        super(message);
    }
}
