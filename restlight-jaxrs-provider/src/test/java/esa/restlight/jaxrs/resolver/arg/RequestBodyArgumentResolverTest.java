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
package esa.restlight.jaxrs.resolver.arg;

import esa.httpserver.core.HttpRequest;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.MethodParam;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.serialize.GsonHttpBodySerializer;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.serialize.JacksonHttpBodySerializer;
import esa.restlight.core.serialize.JacksonSerializer;
import esa.restlight.core.util.MediaType;
import esa.restlight.jaxrs.ResolverUtils;
import esa.restlight.jaxrs.resolver.Pojo;
import esa.restlight.test.mock.MockHttpRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DefaultValue;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestBodyArgumentResolverTest {

    private static RequestBodyArgumentResolver resolverFactory;

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        resolverFactory = new RequestBodyArgumentResolver();
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testSupportIfAnnotationAbsent() throws Exception {
        final Pojo origin = new Pojo(1024, "hello restlight");
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolved = createResolverAndResolve(request, "none");
        assertEquals(origin, resolved);
    }

    @Test
    void testDefautlValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultValue");
        assertEquals("default", resolved);
    }

    @Test
    void testMultiSerializer() throws Exception {

        final MethodParam parameter = handlerMethods.get("none").parameters()[0];

        List<HttpRequestSerializer> serializers = Arrays.asList(new GsonHttpBodySerializer() {
            @Override
            public boolean supportsRead(MediaType mediaType, Type type) {
                return mediaType.isCompatibleWith(MediaType.APPLICATION_XML);
            }
        }, new JacksonHttpBodySerializer());
        ArgumentResolver resolver = new RequestBodyArgumentResolver()
                .createResolver(parameter, serializers);

        final Pojo origin = new Pojo(1024, "hello restlight");
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolvedWithJson = resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
        assertEquals(origin, resolvedWithJson);
        final HttpRequest request2 = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_XML.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolvedWithXml = resolver.resolve(request2, MockAsyncResponse.aMockResponse().build());

        assertEquals(origin, resolvedWithXml);
    }

    private static Object createResolverAndResolve(HttpRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(parameter));
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter,
                Collections.singletonList(new JacksonHttpBodySerializer()));
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {

        public void none(Pojo pojo) {
        }


        public void defaultValue(@DefaultValue("default") String pojoStr) {
        }

    }

}
