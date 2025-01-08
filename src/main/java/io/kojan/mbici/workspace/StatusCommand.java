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
package io.kojan.mbici.workspace;

import io.kojan.mbici.AbstractCommand;
import io.kojan.mbici.Main;
import io.kojan.mbici.cache.ArtifactType;
import io.kojan.workflow.model.Artifact;
import io.kojan.workflow.model.Result;
import io.kojan.workflow.model.TaskOutcome;
import io.kojan.workflow.model.Workflow;
import java.util.List;
import java.util.stream.Collectors;
import picocli.CommandLine.Command;

@Command(
        name = "status",
        description = "Display workspace status.",
        mixinStandardHelpOptions = true,
        versionProvider = Main.class)
public class StatusCommand extends AbstractCommand {

    @Override
    public Integer call() throws Exception {

        Workspace ws = Workspace.findOrAbort();
        WorkspaceConfig c = ws.getConfig();
        Workflow workflow = Workflow.readFromXML(c.getWorkflowPath());

        List<Result> failed =
                workflow.getResults().stream()
                        .filter(result -> result.getOutcome() != TaskOutcome.SUCCESS)
                        .collect(Collectors.toList());

        if (!failed.isEmpty()) {
            info("Workflow outcome: FAILED");
        } else if (workflow.getResults().size() == workflow.getTasks().size()) {
            info("Workflow outcome: PASSED");
        } else {
            info("Workflow outcome: STILL RUNNING");
        }

        if (!failed.isEmpty()) {
            info("Failed tasks:");
            for (Result result : failed) {
                info("  - task: " + result.getTaskId());
                info("    reason: " + result.getOutcomeReason());
                info("    logs:");
                for (Artifact artifact : result.getArtifacts()) {
                    if (artifact.getType().equals(ArtifactType.LOG)
                            || artifact.getType().equals(ArtifactType.CONFIG)) {
                        info(
                                "      - "
                                        + c.getLinkDir()
                                                .resolve(result.getTaskId())
                                                .resolve(artifact.getName()));
                    }
                }
            }
        }

        return 0;
    }
}
