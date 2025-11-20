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
import io.kojan.workflow.model.Parameter;
import io.kojan.workflow.model.Task;
import io.kojan.workflow.model.Workflow;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "shell",
        description = "Generate and execute MBI workflow from YAML definition.",
        mixinStandardHelpOptions = true,
        versionProvider = Main.class)
public class ShellCommand extends AbstractCommand {
    @Option(
            names = {"-p", "--provision"},
            description = "Provision mock container before runinng shell.")
    private boolean provision;

    @Parameters(index = "0", arity = "0..1", description = "Unique identifier of this shell run.")
    private String id = "shell";

    private Guest guest;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String guessResultId(Task task, String dependencyResultId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(task.getHandler().getBytes());
            md.update(Byte.MIN_VALUE);
            for (Parameter param : task.getParameters()) {
                md.update(param.getName().getBytes());
                md.update(Byte.MIN_VALUE);
                md.update(param.getValue().getBytes());
                md.update(Byte.MIN_VALUE);
            }
            if (dependencyResultId != null) {
                md.update(dependencyResultId.getBytes());
                md.update(Byte.MIN_VALUE);
            }
            byte[] digest = md.digest();
            return new BigInteger(1, digest)
                    .setBit(digest.length << 3)
                    .toString(16)
                    .substring(1)
                    .toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public Integer provision() throws Exception {

        Workspace ws = Workspace.findOrAbort();
        WorkspaceConfig c = ws.getConfig();
        info("Using workspace at " + ws.getWorkspaceDir());

        Path composeRepoDir = AbstractTmtCommand.findComposeOrAbort(ws);

        Files.createDirectories(c.getTestResultDir());
        Files.createDirectories(c.getWorkDir());

        Path yamlPath = ws.getWorkspaceDir().resolve("mbi.yaml");
        YamlConf yaml = YamlConf.load(yamlPath);
        WorkflowFactory wff = new WorkflowFactory();
        Workflow wf = wff.createTestWorkflow(id, yaml.getTestPlatform(), composeRepoDir);

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
            ProvisionTaskHandler handler = ProvisionTaskHandler.getInstance();
            if (handler != null) {
                guest = handler.getGuest();
                if (guest != null && guest.isSshInitialized()) {
                    System.err.println("\r======================================================");
                    break;
                }
            }
            Thread.sleep(Duration.ofMillis(100));
        }

        return 0;
    }

    public void connect() throws Exception {
        guest.runSshClient();
    }

    public void terminate() throws Exception {
        guest.runSshClient("kill $(cat /tmp/sshd.pid)");
    }

    public Path getSocketPath() throws IOException {
        return guest.getSocketPath();
    }

    @Override
    public Integer call() throws Exception {
        if (provision) {
            Integer ret = provision();
            if (ret != 0) {
                return ret;
            }
        } else {
            Workspace ws = Workspace.findOrAbort();
            Path composeRepoDir = AbstractTmtCommand.findComposeOrAbort(ws);
            Path yamlPath = ws.getWorkspaceDir().resolve("mbi.yaml");
            YamlConf yaml = YamlConf.load(yamlPath);
            WorkflowFactory wff = new WorkflowFactory();
            Workflow wf = wff.createTestWorkflow(id, yaml.getTestPlatform(), composeRepoDir);
            Task testPlatformTask = null;
            Task testRepoTask = null;
            Task provisionTask = null;
            for (Task task : wf.getTasks()) {
                if (task.getId().equals("test-platform")) {
                    testPlatformTask = task;
                }
                if (task.getId().equals("test-platform-repo")) {
                    testRepoTask = task;
                }
                if (task.getId().equals("provision-" + id)) {
                    provisionTask = task;
                }
            }
            if (testPlatformTask == null || testRepoTask == null || provisionTask == null) {
                error("Unable to find expected tasks in test workflow");
                return 1;
            }
            String testPlatformResultId = guessResultId(testPlatformTask, null);
            String testRepoResultId = guessResultId(testRepoTask, testPlatformResultId);
            String provisionId = guessResultId(provisionTask, testRepoResultId);
            Path provisionWorkDir =
                    ws.getConfig().getWorkDir().resolve(provisionTask.getId()).resolve(provisionId);
            guest = new Guest(provisionWorkDir);
            if (!guest.isSshInitialized()) {
                error("Provision " + provisionTask.getId() + " is not active");
                return 1;
            }
        }
        connect();
        if (provision) {
            terminate();
        }
        return 0;
    }
}
