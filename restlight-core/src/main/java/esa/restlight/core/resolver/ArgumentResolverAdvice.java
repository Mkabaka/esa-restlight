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
package esa.restlight.core.resolver;

import esa.httpserver.core.HttpRequest;
import esa.httpserver.core.HttpResponse;

/**
 * Allows customising the {@link HttpRequest} before resolving the parameter of controller from {@link HttpRequest}
 * and customizing the argument resoled from {@link HttpRequest}.
 */
public interface ArgumentResolverAdvice {

    /**
     * This method will be called before {@link ArgumentResolver#resolve(HttpRequest, HttpResponse)} method.
     *
     * @param request  request
     * @param response response
     */
    void beforeResolve(HttpRequest request, HttpResponse response);

    /**
     * This method will be called after {@link ArgumentResolver#resolve(HttpRequest, HttpResponse)} method, and allows
     * customising the argument resolved by {@link ArgumentResolver#resolve(HttpRequest, HttpResponse)} method which
     * will be passed as the first parameter of this method.
     *
     * @param arg      argument resolved by {@link ArgumentResolver}
     * @param request  request
     * @param response response
     * @return argument that was passed or a modified(possibly new) instance.
     */
    Object afterResolved(Object arg, HttpRequest request, HttpResponse response);
}
