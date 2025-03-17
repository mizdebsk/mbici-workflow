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
import io.kojan.mbici.tasks.Guest;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.Option;

public abstract class AbstractTmtCommand extends AbstractCommand {

    @Option(
            names = {"-e", "--environment"},
            description = "Environment to pass to tmt, in format key=val.")
    private List<String> environment = new ArrayList<>();

    @Option(
            names = {"-c", "--context"},
            description = "Context to pass to tmt, in format key=val.")
    private List<String> context = new ArrayList<>();

    protected abstract String getTestPlan();

    protected abstract boolean requiresGuest();

    @Override
    public Integer call() throws Exception {

        ShellCommand shell = null;
        if (requiresGuest()) {
            shell = new ShellCommand();
            Integer ret = shell.provision();
            if (ret != 0) {
                return ret;
            }
        }

        String testPlan = getTestPlan();
        if (testPlan.startsWith("/")) {
            testPlan = testPlan.substring(1);
        }

        Workspace ws = Workspace.findOrAbort();
        WorkspaceConfig c = ws.getConfig();
        info("Using workspace at " + ws.getWorkspaceDir());

        if (!Files.isDirectory(c.getComposeDir())) {
            error("Compose is absent");
            return 1;
        }

        List<String> cmd = new ArrayList<>();
        cmd.add("tmt");
        if (!requiresGuest()) {
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
        cmd.add("TEST_ARTIFACTS=" + c.getComposeDir());
        for (String env : environment) {
            cmd.add("-e");
            cmd.add(env);
        }

        cmd.add("plans");
        cmd.add("--name");
        cmd.add("^/" + testPlan + "$");

        if (requiresGuest()) {
            cmd.add("provision");
            cmd.add("--how");
            cmd.add("connect");
            cmd.add("--guest");
            cmd.add(Guest.SSH_HOST);
            cmd.add("--port");
            cmd.add(Guest.SSH_PORT);
            cmd.add("--user");
            cmd.add(Guest.SSH_USER);
            cmd.add("--key");
            cmd.add(Guest.SSH_PRIV_KEY);
            cmd.add("-vvv");
        } else {
            cmd.add("provision");
            cmd.add("--how");
            cmd.add("local");
        }

        cmd.add("execute");
        cmd.add("-vvv");

        cmd.add("report");
        cmd.add("-vv");

        int ret = new ProcessBuilder(cmd).inheritIO().start().waitFor();
        if (shell != null) {
            shell.terminate();
        }
        return ret;
    }
}
