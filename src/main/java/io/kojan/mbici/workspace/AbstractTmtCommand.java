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
import io.kojan.workflow.model.Result;
import io.kojan.workflow.model.Workflow;
import io.kojan.xml.XMLException;
import java.io.IOException;
import java.nio.file.Path;
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

    public static Path findComposeOrAbort(Workspace ws) throws IOException, XMLException {
        WorkspaceConfig c = ws.getConfig();
        Workflow wf = Workflow.readFromXML(c.getWorkflowPath());
        for (Result result : wf.getResults()) {
            if (result.getTaskId().equals("compose")) {
                return c.getResultDir().resolve("compose").resolve(result.getId()).resolve("repo");
            }
        }
        throw new RuntimeException("This command requires a compose");
    }

    @Override
    public Integer call() throws Exception {

        ShellCommand shell = null;
        if (requiresGuest()) {
            shell = new ShellCommand();
            shell.setId("test-" + getTestPlan());
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

        Path composeRepoDir = findComposeOrAbort(ws);

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
        cmd.add("TEST_ARTIFACTS=" + composeRepoDir);
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
            cmd.add("dummy");
            cmd.add("--user");
            cmd.add("root");
            cmd.add("--ssh-option");
            cmd.add("ProxyCommand=socat - UNIX-CONNECT:" + shell.getSocketPath());
            cmd.add("--ssh-option");
            cmd.add("PreferredAuthentications=password");
            cmd.add("--ssh-option");
            cmd.add("PubkeyAuthentication=no");
            cmd.add("--ssh-option");
            cmd.add("KbdInteractiveAuthentication=no");
            cmd.add("--ssh-option");
            cmd.add("GSSAPIAuthentication=no");
            cmd.add("--ssh-option");
            cmd.add("UserKnownHostsFile=/dev/null");
            cmd.add("--ssh-option");
            cmd.add("StrictHostKeyChecking=no");
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
