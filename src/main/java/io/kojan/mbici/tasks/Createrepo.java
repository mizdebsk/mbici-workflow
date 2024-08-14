/*-
 * Copyright (c) 2023 Red Hat, Inc.
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

import java.nio.file.Path;

import io.kojan.workflow.TaskExecution;
import io.kojan.workflow.TaskTermination;

/**
 * @author Mikolaj Izdebski
 */
class Createrepo {
    private static final int CREATEREPO_TIMEOUT = 60;

    private final TaskExecution taskExecution;

    public Createrepo(TaskExecution taskExecution) {
        this.taskExecution = taskExecution;
    }

    public void run(Path repoPath) throws TaskTermination {
        Command createrepo = new Command("createrepo_c", repoPath.toString());
        createrepo.setName("createrepo");
        createrepo.runRemote(taskExecution, CREATEREPO_TIMEOUT);
    }
}
