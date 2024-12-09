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

    protected abstract String getImage();

    protected abstract String getPlaybook();

    @Override
    public Integer call() throws Exception {

        String testPlan = getTestPlan();
        if (testPlan.startsWith("/")) {
            testPlan = testPlan.substring(1);
        }

        Workspace ws = Workspace.findOrAbort();
        WorkspaceConfig c = ws.getConfig();
        info("Using workspace at " + ws.getWorkspaceDir());

        List<String> cmd = new ArrayList<>();
        cmd.add("tmt");
        if (getImage() == null) {
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
        cmd.add("/" + testPlan);

        if (getImage() != null) {
            cmd.add("provision");
            cmd.add("--how");
            cmd.add("container");
            cmd.add("--image");
            cmd.add(getImage());
        } else {
            cmd.add("provision");
            cmd.add("--how");
            cmd.add("local");
        }

        if (getPlaybook() != null) {
            cmd.add("prepare");
            cmd.add("--insert");
            cmd.add("--order");
            cmd.add("0");
            cmd.add("--name");
            cmd.add("mbici");
            cmd.add("--how");
            cmd.add("ansible");
            cmd.add("--playbook");
            cmd.add(getPlaybook());
            cmd.add("-vvv");
        }

        cmd.add("execute");
        cmd.add("-vvv");

        cmd.add("report");
        cmd.add("-vv");

        return new ProcessBuilder(cmd).inheritIO().start().waitFor();
    }
}
