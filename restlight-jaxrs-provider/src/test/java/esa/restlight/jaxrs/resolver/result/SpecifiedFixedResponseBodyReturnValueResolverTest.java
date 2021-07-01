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
package esa.restlight.jaxrs.resolver.result;

import esa.httpserver.core.HttpRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.annotation.ResponseSerializer;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.core.serialize.*;
import esa.restlight.core.util.MediaType;
import esa.restlight.jaxrs.ResolverUtils;
import esa.restlight.jaxrs.resolver.Pojo;
import esa.restlight.test.mock.MockHttpRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpecifiedFixedResponseBodyReturnValueResolverTest {

    private static SpecifiedFixedResponseBodyReturnValueResolver resolverFactory;

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        resolverFactory = new SpecifiedFixedResponseBodyReturnValueResolver();
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testSupport() {
        final InvocableMethod absent = handlerMethods.get("none");
        assertFalse(resolverFactory.supports(absent));

        final InvocableMethod absent1 = handlerMethods.get("responseBody");
        assertFalse(resolverFactory.supports(absent1));

        final InvocableMethod illegal = handlerMethods.get("illegal");
        assertFalse(resolverFactory.supports(illegal));

        final InvocableMethod support = handlerMethods.get("jackson");
        assertTrue(resolverFactory.supports(support));
    }

    @Test
    void testResolve() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Pojo pojo = new Pojo(1024, "foo");
        final byte[] resolved = createMultiResolverAndResolve(pojo, request, response, "jackson");
        assertArrayEquals(JacksonSerializer.getDefaultMapper().writeValueAsBytes(pojo), resolved);
    }

    @Test
    void testResolveDetectableStringType() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final String foo = "foo";
        final byte[] resolved = createMultiResolverAndResolve(foo, request, response, "str");
        assertArrayEquals(foo.getBytes(StandardCharsets.UTF_8), resolved);
    }

    @Test
    void testResolveDetectableByteArrayType() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final byte[] foo = "foo".getBytes(StandardCharsets.UTF_8);
        final byte[] resolved = createMultiResolverAndResolve(foo, request, response, "byteArray");
        assertArrayEquals(foo, resolved);
    }

    @Test
    void testResolveDetectableByteBufType() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final ByteBuf foo = Unpooled.copiedBuffer("foo".getBytes(StandardCharsets.UTF_8));
        final byte[] resolved = createMultiResolverAndResolve(foo, request, response, "byteBuf");
        assertArrayEquals(Serializers.alreadyWrite(), resolved);
    }

    @Test
    void testResolveDetectablePrimitiveType() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final int foo = 1;
        final byte[] resolved = createMultiResolverAndResolve(foo, request, response, "byteBuf");
        assertArrayEquals(String.valueOf(foo).getBytes(StandardCharsets.UTF_8), resolved);
    }

    private static byte[] createMultiResolverAndResolve(Object returnValue, HttpRequest request,
                                                        AsyncResponse response,
                                                        String method) throws Exception {
        final InvocableMethod invocableMethod = handlerMethods.get(method);
        assertTrue(resolverFactory.supports(invocableMethod));
        final ReturnValueResolver resolver = resolverFactory.createResolver(invocableMethod,
                Arrays.asList(new JacksonHttpBodySerializer() {
                    @Override
                    public int getOrder() {
                        return LOWEST_PRECEDENCE;
                    }
                }, new ProtoBufHttpBodySerializer() {
                    @Override
                    public int getOrder() {
                        return HIGHEST_PRECEDENCE;
                    }
                }));
        return resolver.resolve(returnValue, request, response);
    }

    private static class Subject {

        public Pojo none() {
            return null;
        }

        public Pojo responseBody() {
            return null;
        }

        @ResponseSerializer(HttpResponseSerializer.class)
        public Pojo illegal() {
            return null;
        }

        @ResponseSerializer(JacksonHttpBodySerializer.class)
        public Pojo jackson() {
            return null;
        }

        @ResponseSerializer(JacksonHttpBodySerializer.class)
        public Object str() {
            return null;
        }

        @ResponseSerializer(JacksonHttpBodySerializer.class)
        public Object byteArray() {
            return null;
        }

        @ResponseSerializer(JacksonHttpBodySerializer.class)
        public Object byteBuf() {
            return null;
        }

        @ResponseSerializer(JacksonHttpBodySerializer.class)
        public Object primitive() {
            return null;
        }
    }

}
