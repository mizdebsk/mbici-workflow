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

import io.kojan.mbici.Main;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.Command;

@Command(
        name = "validate",
        description = "Run javapackages-validator on built packages.",
        mixinStandardHelpOptions = true,
        versionProvider = Main.class)
public class ValidateCommand extends AbstractTmtCommand {

    private final String planName = "validate";

    @Override
    public Integer call() throws Exception {

        String testPlan = planName;
        if (testPlan.startsWith("/")) {
            testPlan = testPlan.substring(1);
        }

        Workspace ws = Workspace.findOrAbort();
        WorkspaceConfig c = ws.getConfig();
        info("Using workspace at " + ws.getWorkspaceDir());

        Path composeRepoDir = findComposeOrAbort(ws);

        List<String> cmd = new ArrayList<>();
        cmd.add("tmt");
        if (!false) {
            cmd.add("--feeling-safe");
        }
        cmd.add("-r");
        cmd.add(c.getTestPlanDir().toString());
        for (String ctx : context) {
            cmd.add("-c");
            cmd.add(ctx);
        }

        cmd.add("run");
        cmd.add("--scratch");
        cmd.add("-a");
        cmd.add("-i");
        cmd.add(c.getTestResultDir().resolve(testPlan).toString());
        cmd.add("-e");
        cmd.add("TEST_ARTIFACTS=" + composeRepoDir);
        for (String env : environment) {
            cmd.add("-e");
            cmd.add(env);
        }

        cmd.add("plans");
        cmd.add("--name");
        cmd.add("^/" + testPlan + "$");

        cmd.add("provision");
        cmd.add("--how");
        cmd.add("local");

        cmd.add("execute");
        cmd.add("-vvv");

        cmd.add("report");
        cmd.add("-vv");

        return new ProcessBuilder(cmd).inheritIO().start().waitFor();
    }
}
