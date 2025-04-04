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

import io.kojan.mbici.cache.ArtifactType;
import io.kojan.workflow.FinishedTask;
import io.kojan.workflow.TaskExecutionContext;
import io.kojan.workflow.TaskTermination;
import io.kojan.workflow.model.Task;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/// @author Mikolaj Izdebski
public class RepoTaskHandler extends AbstractTaskHandler {
    public RepoTaskHandler(Task task) {
        if (!task.getParameters().isEmpty()) {
            throw new IllegalArgumentException(
                    getClass().getName() + " does not take any parameters");
        }
    }

    @Override
    public void handleTask(TaskExecutionContext context) throws TaskTermination {
        Path repoPath = context.addArtifact(ArtifactType.REPO, "repo");
        try {
            Files.createDirectories(repoPath);
        } catch (IOException e) {
            TaskTermination.error(
                    "I/O error when creating directory " + repoPath + ": " + e.getMessage());
        }

        Set<Path> rpmPaths = new LinkedHashSet<>();
        rpmPaths.addAll(context.getDependencyArtifacts(ArtifactType.RPM));
        if (context.getDependencies().stream()
                .map(FinishedTask::getTask)
                .map(Task::getHandler)
                .anyMatch(SrpmTaskHandler.class.getName()::equals)) {
            rpmPaths.addAll(context.getDependencyArtifacts(ArtifactType.SRPM));
        }
        for (Path rpmPath : rpmPaths) {
            Path rpmLinkPath = repoPath.resolve(rpmPath.getFileName());

            try {
                Files.createSymbolicLink(rpmLinkPath, rpmPath);
            } catch (IOException e) {
                TaskTermination.error(
                        "I/O error when creating symbolic link "
                                + rpmLinkPath
                                + ": "
                                + e.getMessage());
            }
        }

        Createrepo createrepo = new Createrepo(context);
        createrepo.run(repoPath);

        TaskTermination.success("Repo created successfully");
    }
}
