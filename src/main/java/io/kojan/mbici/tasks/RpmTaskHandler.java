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
import io.kojan.workflow.TaskExecutionContext;
import io.kojan.workflow.TaskTermination;
import io.kojan.workflow.model.Parameter;
import io.kojan.workflow.model.Task;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Mikolaj Izdebski
 */
public class RpmTaskHandler extends AbstractTaskHandler {
    private final List<Parameter> macros;

    public RpmTaskHandler(Task task) {
        macros = task.getParameters();
    }

    @Override
    public void handleTask(TaskExecutionContext context) throws TaskTermination {
        Path srpmPath = context.getDependencyArtifact(ArtifactType.SRPM);
        Mock mock = new Mock();
        for (Parameter param : macros) {
            mock.addMacro(param.getName(), param.getValue());
        }
        mock.run(context, "--rebuild", srpmPath.toString());
        try (var s =
                Files.find(
                        context.getResultDir(),
                        1,
                        (p, bfa) ->
                                p.getFileName().toString().endsWith(".rpm")
                                        && !p.getFileName().toString().endsWith(".src.rpm")
                                        && bfa.isRegularFile())) {
            for (var it = s.iterator(); it.hasNext(); ) {
                context.addArtifact(ArtifactType.RPM, it.next().getFileName().toString());
            }
        } catch (IOException e) {
            throw TaskTermination.error("I/O error when looknig for RPM files: " + e.getMessage());
        }
        TaskTermination.success("Binary RPMs were built in mock");
    }
}
