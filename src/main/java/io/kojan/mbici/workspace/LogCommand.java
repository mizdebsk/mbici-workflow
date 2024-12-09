/*-
 * Copyright (c) 2024 Red Hat, Inc.
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

import io.kojan.mbici.model.Phase;
import io.kojan.mbici.model.Plan;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "log",
        description = "Display or locate build logs or other task artifacts.",
        mixinStandardHelpOptions = true)
public class LogCommand extends AbstractCommand {

    @Option(
            names = {"-P", "--path"},
            description = "Print path to the log instead of displaying log contents.")
    private boolean printPath;

    @Option(
            names = {"--no-pager"},
            description = "Dump the log to stdout instead of using a pager to display it.")
    private boolean noPager;

    @Option(
            names = {"-p", "--phase"},
            description = "Print logs of given phase.")
    private String phase;

    @Option(
            names = {"-r", "--root"},
            description = "Display root.log instead of build.log.")
    private boolean rootLog;

    @Option(
            names = {"-a", "--artifact"},
            description = "Print custom artifact instead of build.log.")
    private String artifact;

    @Option(
            names = {"-s", "--srpm"},
            description = "Print SRPM task logs instead of RPM task logs.")
    private boolean srpm;

    @Parameters(
            index = "0",
            arity = "0..1",
            description = "The component to print logs of, can be auto-guessed.")
    private String component;

    @Override
    public Integer call() throws Exception {

        Workspace ws = Workspace.findOrAbort();
        WorkspaceConfig c = ws.getConfig();

        Path cwd = Path.of(".").toAbsolutePath();
        if (component == null) {
            for (Path p = cwd, pp = p.getParent(); pp != null; p = pp, pp = pp.getParent()) {
                if (pp.equals(c.getScmDir())) {
                    component = p.getFileName().toString();
                    break;
                }
            }
        }
        if (component == null) {
            error("Unable to determine component to show logs of");
            return 1;
        }

        if (phase == null && !srpm) {
            Plan plan = Plan.readFromXML(c.getPlanPath());
            List<String> phases =
                    plan.getPhases().stream()
                            .filter(phase -> phase.getComponents().contains(component))
                            .map(Phase::getName)
                            .toList();
            if (phases.isEmpty()) {
                error("Component " + component + " is not part of any phase");
                return 1;
            }
            if (phases.size() > 1) {
                error("Ambigous phase for component " + component);
                info("Please specify explicit phase with --phase or -p");
                info("The component belongs to phases: " + phases);
                return 1;
            }
            phase = phases.getFirst();
        }

        Path resDir = c.getLinkDir();
        Path taskDir = resDir.resolve(component + (srpm ? "-srpm" : "-" + phase + "-rpm"));
        Path logPath =
                taskDir.resolve(artifact != null ? artifact : rootLog ? "root.log" : "build.log");

        if (!Files.isRegularFile(logPath)) {
            error("Unable to find " + logPath.getFileName());
            info("It was expected to be found at " + logPath);
            return 1;
        }

        if (printPath) {
            System.out.println(logPath);
            return 0;
        }

        String pager = noPager ? "cat" : "less";

        return new ProcessBuilder(pager, logPath.toString()).inheritIO().start().waitFor();
    }
}
