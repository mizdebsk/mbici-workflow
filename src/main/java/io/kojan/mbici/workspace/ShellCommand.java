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
import io.kojan.mbici.execute.TaskHandlerFactoryImpl;
import io.kojan.mbici.generate.WorkflowFactory;
import io.kojan.mbici.tasks.Guest;
import io.kojan.mbici.tasks.ProvisionTaskHandler;
import io.kojan.workflow.TaskHandlerFactory;
import io.kojan.workflow.TaskStorage;
import io.kojan.workflow.TaskThrottle;
import io.kojan.workflow.WorkflowExecutor;
import io.kojan.workflow.model.Task;
import io.kojan.workflow.model.Workflow;
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

        Path composeRepoDir =
                Files.readSymbolicLink(c.getLinkDir().resolve("compose")).resolve("repo");
        if (!Files.isDirectory(composeRepoDir)) {
            error("Compose is absent");
            info("You should run the \"mbi run\" command to generate the compose");
            return 1;
        }

        Files.createDirectories(c.getTestResultDir());
        Files.createDirectories(c.getWorkDir());

        Path yamlPath = ws.getWorkspaceDir().resolve("mbi.yaml");
        YamlConf yaml = YamlConf.load(yamlPath);
        WorkflowFactory wff = new WorkflowFactory();
        Workflow wf = wff.createTestWorkflow(yaml.getTestPlatform(), composeRepoDir);

        TaskHandlerFactory handlerFactory = new TaskHandlerFactoryImpl(null);
        TaskThrottle throttle =
                new TaskThrottle() {
                    public void releaseCapacity(Task task) {}

                    public void acquireCapacity(Task task) {}
                };
        TaskStorage storage =
                new TaskStorage() {

                    public Path getResultDir(Task task, String resultId) {
                        return c.getTestResultDir().resolve(task.getId()).resolve(resultId);
                    }

                    public Path getWorkDir(Task task, String resultId) {
                        return c.getWorkDir().resolve(task.getId()).resolve(resultId);
                    }
                };
        WorkflowExecutor wfe = new WorkflowExecutor(wf, handlerFactory, storage, throttle, false);

        Thread thread =
                new Thread(
                        () -> {
                            try {
                                wfe.execute();
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
