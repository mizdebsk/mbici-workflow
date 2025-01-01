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

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "config",
        description = "Display or update MBI workspace configuration.",
        mixinStandardHelpOptions = true)
public class ConfigCommand extends AbstractConfigCommand {

    @Option(
            names = {"-s", "--show"},
            description = "Display configuration in human-readable form.")
    private boolean show;

    @Option(
            names = {"--env"},
            description = "Dump shell code for setting configuration as shell variables.")
    private boolean env;

    private void printEnv(String key, Object value) {
        System.out.println(key + "=\"" + value + "\"");
    }

    private void printHuman(int n, String key, Object value) {
        String padding = " ".repeat(Math.max(n - key.length(), 0));
        System.out.println("  " + cs.optionText(key) + padding + " : " + value);
    }

    @Override
    public Integer call() throws Exception {
        Workspace ws = Workspace.findOrAbort();
        WorkspaceConfig c = ws.getConfig();

        boolean updated = updateConfig(c);
        if (updated) {
            success("Configuration updated");
            ws.write();
        }

        if (env) {
            printEnv("mbiSubjectPath", c.getSubjectPath());
            printEnv("mbiWorkflowPath", c.getWorkflowPath());
            printEnv("mbiPlanPath", c.getPlanPath());
            printEnv("mbiPlatformPath", c.getPlatformPath());
            printEnv("mbiResultDir", c.getResultDir());
            printEnv("mbiCacheDir", c.getCacheDir());
            printEnv("mbiWorkDir", c.getWorkDir());
            printEnv("mbiLinkDir", c.getLinkDir());
            printEnv("mbiReportDir", c.getReportDir());
            printEnv("mbiComposeDir", c.getComposeDir());
            printEnv("mbiTestPlanDir", c.getTestPlanDir());
            printEnv("mbiTestResultDir", c.getTestResultDir());
            printEnv("mbiLookaside", c.getLookaside());
            printEnv("mbiScmDir", c.getScmDir());
            printEnv("mbiScmRef", c.getScmRef());
            return 0;
        }

        if (show || !updated) {
            System.out.println("Directory structure:");
            printHuman(15, "subject path", c.getSubjectPath());
            printHuman(15, "workflow path", c.getWorkflowPath());
            printHuman(15, "plan path", c.getPlanPath());
            printHuman(15, "platform path", c.getPlatformPath());
            printHuman(15, "result dir", c.getResultDir());
            printHuman(15, "cache dir", c.getCacheDir());
            printHuman(15, "work dir", c.getWorkDir());
            printHuman(15, "link dir", c.getLinkDir());
            printHuman(15, "report dir", c.getReportDir());
            printHuman(15, "compose dir", c.getComposeDir());
            printHuman(15, "test plan dir", c.getTestPlanDir());
            printHuman(15, "test result dir", c.getTestResultDir());
            System.out.println("SCM config:");
            printHuman(9, "lookaside", c.getLookaside());
            printHuman(9, "SCM dir", c.getScmDir());
            printHuman(9, "SCM ref", c.getScmRef());
            System.out.println("Limits:");
            printHuman(18, "max checkout tasks", c.getMaxCheckoutTasks());
            printHuman(18, "max SRPM tasks", c.getMaxSrpmTasks());
            printHuman(18, "max RPM tasks", c.getMaxRpmTasks());
        }

        return 0;
    }
}
