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
import io.kojan.mbici.execute.AbstractExecuteCommand;
import io.kojan.mbici.execute.LocalExecuteCommand;
import io.kojan.mbici.generate.GenerateCommand;
import io.kojan.mbici.subject.LocalSubjectCommand;
import io.kojan.mbici.tasks.Guest;
import io.kojan.mbici.tasks.ProvisionTaskHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import picocli.CommandLine.Command;

@Command(
        name = "shell",
        description = "Generate and execute MBI workflow from YAML definition.",
        mixinStandardHelpOptions = true,
        versionProvider = Main.class)
public class ShellCommand extends AbstractCommand {

    private Guest getGuest() {
        ProvisionTaskHandler handler = ProvisionTaskHandler.getInstance();
        if (handler != null) {
            return handler.getGuest();
        }
        return null;
    }

    public Integer provision() throws Exception {

        Workspace ws = Workspace.findOrAbort();
        WorkspaceConfig c = ws.getConfig();
        info("Using workspace at " + ws.getWorkspaceDir());

        Files.createDirectories(c.getCacheDir());
        Files.createDirectories(c.getResultDir());
        Files.createDirectories(c.getWorkDir());
        Files.createDirectories(c.getLinkDir());

        Path yamlPath = ws.getWorkspaceDir().resolve("mbi.yaml");
        YamlConf yaml = YamlConf.load(yamlPath);
        yaml.getPlan().writeToXML(c.getPlanPath());
        yaml.getPlatform().writeToXML(c.getPlatformPath());
        yaml.getTestPlatform().writeToXML(c.getTestPlatformPath());

        LocalSubjectCommand subject = new LocalSubjectCommand();
        subject.setSubjectPath(c.getSubjectPath());
        subject.setPlanPath(c.getPlanPath());
        subject.setLookaside(c.getLookaside());
        subject.setScmPath(c.getScmDir());
        subject.setRef(c.getScmRef());

        info("Running local-subject command...");
        int ret = subject.call();
        if (ret != 0) {
            error("The local-subject command failed");
            return ret;
        }
        GenerateCommand generate = new GenerateCommand();
        generate.setPlanPath(c.getPlanPath());
        generate.setPlatformPath(c.getPlatformPath());
        generate.setTestPlatformPath(c.getTestPlatformPath());
        generate.setSubjectPath(c.getSubjectPath());
        generate.setWorkflowPath(c.getWorkflowPath());

        info("Running generate command...");
        ret = generate.call();
        if (ret != 0) {
            error("The generate command failed");
            return ret;
        }

        AbstractExecuteCommand execute = new LocalExecuteCommand();
        execute.setWorkflowPath(c.getWorkflowPath());
        execute.setResultDir(c.getResultDir());
        execute.setCacheDir(c.getCacheDir());
        execute.setWorkDir(c.getWorkDir());
        execute.setLinkerDir(c.getLinkDir());
        execute.setMaxCheckoutTasks(c.getMaxCheckoutTasks());
        execute.setMaxSrpmTasks(c.getMaxSrpmTasks());
        execute.setMaxRpmTasks(c.getMaxRpmTasks());
        execute.setBatchMode(false);

        Thread thread =
                new Thread(
                        () -> {
                            try {
                                info("Running execute command...");
                                int retCode = execute.call();
                                if (retCode != 0) {
                                    error("The execute command failed");
                                    return;
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
        thread.setDaemon(true);
        thread.start();

        while (true) {
            Guest guest = getGuest();
            if (guest != null && guest.isSshInitialized()) {
                System.err.println("\r======================================================");
                break;
            }
            Thread.sleep(Duration.ofMillis(100));
        }

        return 0;
    }

    public void connect() throws Exception {
        getGuest().runSshClient();
    }

    public void terminate() throws Exception {
        getGuest().runSshClient("kill $(cat /tmp/sshd.pid)");
    }

    @Override
    public Integer call() throws Exception {
        Integer ret = provision();
        if (ret != 0) {
            return ret;
        }
        connect();
        terminate();
        return 0;
    }
}
