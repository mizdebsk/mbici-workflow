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

import io.kojan.workflow.FinishedTask;
import io.kojan.workflow.WorkflowExecutionListener;
import io.kojan.workflow.model.Artifact;
import io.kojan.workflow.model.Task;
import io.kojan.workflow.model.Workflow;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * @author Mikolaj Izdebski
 */
public class Linker implements WorkflowExecutionListener {
    private final Path linkDir;

    public Linker(Path linkDir) {
        this.linkDir = linkDir;
    }

    @Override
    public void taskRunning(Workflow workflow, Task task) {
        // Nothing to do
    }

    @Override
    public void taskSucceeded(Workflow workflow, FinishedTask finishedTask) {
        try {
            Path link = linkDir.resolve(finishedTask.getTask().getId());
            Path target = finishedTask.getArtifact(new Artifact("dummy", "dummy")).getParent();
            if (Files.exists(link, LinkOption.NOFOLLOW_LINKS)) {
                Files.delete(link);
            }
            Files.createSymbolicLink(link, target);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void taskFailed(Workflow workflow, FinishedTask finishedTask) {
        taskSucceeded(workflow, finishedTask);
    }

    @Override
    public void taskReused(Workflow workflow, FinishedTask finishedTask) {
        taskSucceeded(workflow, finishedTask);
    }

    @Override
    public void workflowRunning(Workflow workflow) {
        // Nothing to do
    }

    @Override
    public void workflowSucceeded(Workflow workflow) {
        // Nothing to do
    }

    @Override
    public void workflowFailed(Workflow workflow) {
        // Nothing to do
    }
}
