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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "init", description = "initialize MBI workspace", mixinStandardHelpOptions = true)
public class InitCommand extends AbstractWorkspaceCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        Workspace ws = Workspace.find();
        if (ws != null) {
            System.err.println("Workspace already exists at path " + ws.getWorkspaceDir());
            return 1;
        }

        Path cwd = Path.of(".").toAbsolutePath().getParent();
        Files.createDirectory(cwd.resolve(".mbi"));

        WorkspaceConfig c = new WorkspaceConfig();
        c.setSubjectPath(cwd.resolve(".mbi").resolve("subject.xml"));
        c.setWorkflowPath(cwd.resolve(".mbi").resolve("workflow.xml"));
        c.setPlanPath(cwd.resolve(".mbi").resolve("plan.xml"));
        c.setPlatformPath(cwd.resolve(".mbi").resolve("platform.xml"));
        c.setResultDir(cwd.resolve(".mbi").resolve("result"));
        c.setCacheDir(cwd.resolve(".mbi").resolve("cache"));
        c.setWorkDir(Path.of("/tmp"));
        c.setLinkDir(cwd.resolve("result"));
        c.setReportDir(cwd.resolve("report"));
        c.setLookaside("");
        c.setScmDir(cwd.resolve("rpms"));
        c.setScmRef("HEAD");
        c.setMaxCheckoutTasks(20);
        c.setMaxSrpmTasks(10);
        c.setMaxRpmTasks(5);

        updateConfig(c);

        ws = Workspace.create(cwd, c);
        ws.write();
        System.err.println("Initialized workspace at " + ws.getWorkspaceDir());
        return 0;
    }
}
