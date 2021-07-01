
/*
 * Copyright 2021 OPPO ESA Stack Project
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
package esa.restlight.server.bootstrap;

import esa.commons.annotation.Internal;
import esa.httpserver.core.HttpRequest;
import esa.httpserver.core.AsyncResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import static esa.restlight.server.util.ErrorDetail.sendErrorResult;

@Internal
public class DefaultDispatcherExceptionHandler implements DispatcherExceptionHandler {

    @Override
    public HandleStatus handleException(HttpRequest request,
                                        AsyncResponse response,
                                        Throwable throwable) {
        final HttpResponseStatus status;
        if (throwable instanceof WebServerException) {
            //400 bad request
            status = ((WebServerException) throwable).status();

        } else {
            //default to 500
            status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        }

        sendErrorResult(request, response, throwable, status);
        return HandleStatus.HANDLED_RETAINED;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

