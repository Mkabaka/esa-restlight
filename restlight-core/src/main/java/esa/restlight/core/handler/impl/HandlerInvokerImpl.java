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
package esa.restlight.core.handler.impl;

import esa.commons.StringUtils;
import esa.commons.reflect.ReflectionUtils;
import esa.httpserver.core.HttpRequest;
import esa.httpserver.core.HttpResponse;
import esa.restlight.core.handler.HandlerInvoker;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.server.bootstrap.WebServerException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Default implementation of {@link HandlerInvoker} which invokes the target handler by reflection.
 */
public class HandlerInvokerImpl implements HandlerInvoker {

    private final InvocableMethod handler;

    public HandlerInvokerImpl(InvocableMethod handler) {
        this.handler = handler;
    }

    @Override
    public Object invoke(HttpRequest request, HttpResponse response, Object[] args) throws Throwable {
        return doInvoke(args);
    }

    private Object doInvoke(Object[] args) throws Throwable {
        final Method method = handler.method();
        ReflectionUtils.makeMethodAccessible(method);
        try {
            return method.invoke(handler.object(), args);
        } catch (IllegalArgumentException ex) {
            String text = (ex.getMessage() != null ? ex.getMessage() : "Illegal argument");
            throw WebServerException.badRequest(getInvocationMessage(text, args), ex);
        } catch (InvocationTargetException ex) {
            // Unwrap for HandlerExceptionResolvers ...
            throw ex.getTargetException();
        }
    }

    private String getInvocationMessage(String text, Object[] resolvedArgs) {
        StringBuilder sb = new StringBuilder(getDetailedMessage(text));
        sb.append("Resolved arguments: \n");
        if (resolvedArgs == null) {
            sb.append("Resolved arguments: null\n");
        } else {
            for (int i = 0; i < resolvedArgs.length; i++) {
                sb.append("[").append(i).append("] ");
                if (resolvedArgs[i] == null) {
                    sb.append("[null] \n");
                } else {
                    sb.append("[type=").append(resolvedArgs[i].getClass().getName()).append("]\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Adds HandlerMethod details such as the bean type and method signature to the message.
     *
     * @param text error message to append the HandlerMethod details to
     */
    private String getDetailedMessage(String text) {
        return StringUtils.concat(text, "\n",
                "HandlerMethod details: \n",
                "Controller [", handler.beanType().getName(), "]\n" +
                        "Method [", handler.method().toGenericString(), "]\n");
    }
}
