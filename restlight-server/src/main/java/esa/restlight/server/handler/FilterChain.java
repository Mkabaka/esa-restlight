/*
 * Copyright 2020 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package esa.restlight.server.handler;

import esa.httpserver.core.HttpRequest;
import esa.httpserver.core.HttpResponse;

import java.util.concurrent.CompletableFuture;

public interface FilterChain {

    /**
     * Invoke the registered {@link Filter}s.
     *
     * @param request  current request
     * @param response current response
     * @return future
     */
    CompletableFuture<Void> doFilter(HttpRequest request, HttpResponse response);

}
