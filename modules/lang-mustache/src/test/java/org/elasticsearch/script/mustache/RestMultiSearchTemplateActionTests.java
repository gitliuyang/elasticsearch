/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.script.mustache;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.indices.breaker.NoneCircuitBreakerService;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.search.RestMultiSearchAction;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.rest.FakeRestChannel;
import org.elasticsearch.test.rest.FakeRestRequest;
import org.elasticsearch.usage.UsageService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.Mockito.mock;

public class RestMultiSearchTemplateActionTests extends ESTestCase {
    private RestController controller;

    public void setUp() throws Exception {
        super.setUp();
        controller = new RestController(Collections.emptySet(), null,
            mock(NodeClient.class),
            new NoneCircuitBreakerService(),
            new UsageService());
        new RestMultiSearchTemplateAction(Settings.EMPTY, controller);
    }

    public void testTypeInPath() {
        String content = "{ \"index\": \"some_index\" } \n" +
            "{\"source\": {\"query\" : {\"match_all\" :{}}}} \n";
        BytesArray bytesContent = new BytesArray(content.getBytes(StandardCharsets.UTF_8));

        RestRequest request = new FakeRestRequest.Builder(xContentRegistry())
            .withMethod(RestRequest.Method.GET)
            .withPath("/some_index/some_type/_msearch/template")
            .withContent(bytesContent, XContentType.JSON)
            .build();

        performRequest(request);
        assertWarnings(RestMultiSearchAction.TYPES_DEPRECATION_MESSAGE);
    }

    public void testTypeInBody() {
        String content = "{ \"index\": \"some_index\", \"type\": \"some_type\" } \n" +
            "{\"source\": {\"query\" : {\"match_all\" :{}}}} \n";
        BytesArray bytesContent = new BytesArray(content.getBytes(StandardCharsets.UTF_8));

        RestRequest request = new FakeRestRequest.Builder(xContentRegistry())
            .withPath("/some_index/_msearch/template")
            .withContent(bytesContent, XContentType.JSON)
            .build();

        performRequest(request);
        assertWarnings(RestMultiSearchAction.TYPES_DEPRECATION_MESSAGE);
    }

    private void performRequest(RestRequest request) {
        RestChannel channel = new FakeRestChannel(request, false, 1);
        ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        controller.dispatchRequest(request, channel, threadContext);
    }
}
