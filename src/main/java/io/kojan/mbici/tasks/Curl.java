/*-
 * Copyright (c) 2021-2025 Red Hat, Inc.
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
package io.kojan.mbici.tasks;

import io.kojan.workflow.TaskExecutionContext;
import io.kojan.workflow.TaskTermination;
import java.nio.file.Path;

/// @author Mikolaj Izdebski
class Curl {
    private static final int CURL_TIMEOUT = 300;

    private final TaskExecutionContext context;
    private int counter;

    public Curl(TaskExecutionContext context) {
        this.context = context;
    }

    public void downloadFile(String url, Path targetPath) throws TaskTermination {
        Command curl =
                new Command("curl", "--http1.1", "-f", "-L", "-o", targetPath.toString(), url);

        if (counter++ > 0) {
            curl.setName("curl-" + counter);
        }

        curl.run(context, CURL_TIMEOUT);
    }
}
