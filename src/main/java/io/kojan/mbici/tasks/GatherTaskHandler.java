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
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mikolaj Izdebski
 */
public class GatherTaskHandler extends AbstractTaskHandler {
    private static final int GATHER_TIMEOUT = 1200;

    private final List<String> packageNames = new ArrayList<>();
    private final Map<String, String> repos = new LinkedHashMap<>();

    public GatherTaskHandler(Task task) {
        for (Parameter param : task.getParameters()) {
            if (param.getName().startsWith("package-")) {
                packageNames.add(param.getValue());
            } else if (param.getName().startsWith("repo-")) {
                repos.put(param.getName().substring(5), param.getValue());
            } else {
                throw new IllegalArgumentException(
                        "Unknown gather task parameter: " + param.getName());
            }
        }
    }

    private void writeDnfConfig(Path dnfConfPath, Map<String, String> repos)
            throws TaskTermination {
        try (BufferedWriter bw = Files.newBufferedWriter(dnfConfPath)) {
            bw.write("[main]\n");
            bw.write("gpgcheck=0\n");
            bw.write("reposdir=/\n");
            bw.write("install_weak_deps=0\n");

            for (var repo : repos.entrySet()) {
                bw.write("\n");
                bw.write("[" + repo.getKey() + "]\n");
                bw.write("name=" + repo.getKey() + "\n");
                bw.write("baseurl=" + repo.getValue() + "\n");
                bw.write("module_hotfixes=1\n");
            }
        } catch (IOException e) {
            TaskTermination.error("I/O error when writing DNF config: " + e.getMessage());
        }
    }

    private void downloadPackages(
            TaskExecutionContext context,
            List<String> packageNames,
            Path downloadDir,
            Path dnfConfPath)
            throws TaskTermination {
        Command dnf = new Command("dnf5");
        dnf.addArg("--assumeyes");
        dnf.addArg("--releasever", "dummy");
        dnf.addArg("--installroot", context.getWorkDir().toString());
        dnf.addArg("--config", dnfConfPath.toString());
        dnf.addArg("download");
        dnf.addArg("--resolve");
        dnf.addArg("--alldeps");
        dnf.addArg("--destdir=" + downloadDir.toString());
        dnf.addArg(packageNames);
        dnf.runRemote(context, GATHER_TIMEOUT);
    }

    @Override
    public void handleTask(TaskExecutionContext context) throws TaskTermination {
        Path dnfConfPath = context.addArtifact(ArtifactType.CONFIG, "dnf.conf");
        writeDnfConfig(dnfConfPath, repos);

        downloadPackages(context, packageNames, context.getResultDir(), dnfConfPath);

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

        TaskTermination.success("Platform repo was downloaded successfully");
    }
}
