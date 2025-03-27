/*-
 * Copyright (c) 2024-2025 Red Hat, Inc.
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
package io.kojan.mbici.execute;

import io.kojan.workflow.model.Workflow;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

/// @author Mikolaj Izdebski
class WebHookDumper extends AbstractDumper {
    private final String url;
    private final String token;
    private final HttpClient client;

    public WebHookDumper(String url, String token) {
        this.url = url;
        this.token = token;

        client =
                HttpClient.newBuilder()
                        .version(Version.HTTP_1_1)
                        .followRedirects(Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
    }

    @Override
    protected void dump(Workflow workflow) throws Exception {
        String xml = workflow.toXML();

        var reqBuilder = HttpRequest.newBuilder();
        reqBuilder.PUT(BodyPublishers.ofString(xml));
        reqBuilder.uri(URI.create(url));
        reqBuilder.timeout(Duration.ofSeconds(60));
        reqBuilder.header("Content-Type", "text/xml");
        if (token != null) {
            reqBuilder.header("Authorization", "Bearer " + token);
        }
        HttpRequest req = reqBuilder.build();

        HttpResponse<String> resp = client.send(req, BodyHandlers.ofString());
        System.err.println("Webhook resp code " + resp.statusCode());

        Thread.sleep(Duration.ofSeconds(10));
    }
}
