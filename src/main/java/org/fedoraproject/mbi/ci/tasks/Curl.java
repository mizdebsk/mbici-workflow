/*-
 * Copyright (c) 2021-2023 Red Hat, Inc.
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
package org.fedoraproject.mbi.ci.tasks;

import java.nio.file.Path;

import org.fedoraproject.mbi.wf.TaskExecution;
import org.fedoraproject.mbi.wf.TaskTermination;

/**
 * @author Mikolaj Izdebski
 */
class Curl {
    private static final int CURL_TIMEOUT = 300;

    private final TaskExecution taskExecution;
    private int counter;

    public Curl(TaskExecution taskExecution) {
        this.taskExecution = taskExecution;
    }

    public void downloadFile(String url, Path targetPath) throws TaskTermination {
        Command curl = new Command("curl", "--http1.1", "-f", "-L", "-o", targetPath.toString(), url);

        if (counter++ > 0) {
            curl.setName("curl-" + counter);
        }

        curl.run(taskExecution, CURL_TIMEOUT);
    }
}
