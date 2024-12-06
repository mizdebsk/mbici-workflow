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

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "config",
        description = "display or update MBI workspace configuration",
        mixinStandardHelpOptions = true)
public class ConfigCommand extends AbstractWorkspaceCommand implements Callable<Integer> {

    @Option(
            names = {"-s", "--show"},
            description = "display configuration")
    private boolean show;

    @Override
    public Integer call() throws Exception {
        Workspace ws = Workspace.findOrAbort();
        boolean updated = updateConfig(ws.getConfig());
        if (updated) {
            System.err.println("Configuration updated");
            ws.write();
        }

        if (show || !updated) {
            WorkspaceConfig c = ws.getConfig();
            System.out.println("Paths:");
            System.out.println("  subject path  : " + c.getSubjectPath());
            System.out.println("  workflow path : " + c.getWorkflowPath());
            System.out.println("  plan path     : " + c.getPlanPath());
            System.out.println("  platform path : " + c.getPlatformPath());
            System.out.println("  result dir    : " + c.getResultDir());
            System.out.println("  cache dir     : " + c.getCacheDir());
            System.out.println("  work dir      : " + c.getWorkDir());
            System.out.println("  link dir      : " + c.getLinkDir());
            System.out.println("  report dir    : " + c.getReportDir());
            System.out.println("SCM config:");
            System.out.println("  lookaside : " + c.getLookaside());
            System.out.println("  SCM dir   : " + c.getScmDir());
            System.out.println("  SCM ref   : " + c.getScmRef());
            System.out.println("Limits:");
            System.out.println("  max checkout tasks : " + c.getMaxCheckoutTasks());
            System.out.println("  max SRPM tasks     : " + c.getMaxSrpmTasks());
            System.out.println("  max RPM tasks      : " + c.getMaxRpmTasks());
        }

        return 0;
    }
}
