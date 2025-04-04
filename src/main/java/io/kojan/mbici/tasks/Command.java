/*-
 * Copyright (c) 2021-2025 Red Hat, Inc.
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
package io.kojan.mbici.tasks;

import io.kojan.mbici.cache.ArtifactType;
import io.kojan.workflow.TaskExecutionContext;
import io.kojan.workflow.TaskTermination;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/// @author Mikolaj Izdebski
public class Command {
    public static Kubernetes kubernetes;

    private String name;
    private final List<String> cmd = new ArrayList<>();

    public Command(String name, String... args) {
        this.name = name;
        addArg(name);
        addArg(args);
    }

    public void addArg(String... args) {
        for (String arg : args) {
            cmd.add(arg);
        }
    }

    public void addArg(List<String> args) {
        cmd.addAll(args);
    }

    public List<String> getArgs() {
        return Collections.unmodifiableList(cmd);
    }

    public void setName(String name) {
        this.name = name;
    }

    private void runImpl(TaskExecutionContext context, int timeoutSeconds, boolean remote)
            throws TaskTermination {
        remote &= kubernetes != null;

        List<String> actualCommand = cmd;
        if (remote) {
            actualCommand = kubernetes.wrapCommand(context, cmd);
        }

        Path logPath = context.addArtifact(ArtifactType.LOG, name + ".log");

        try (BufferedWriter bw = Files.newBufferedWriter(logPath, StandardOpenOption.CREATE_NEW)) {
            String intro =
                    remote ? "Running remote command on Kubernetes" : "Running local command";
            bw.write(intro + ": " + String.join(" ", cmd) + "\n\n");
        } catch (IOException e) {
            TaskTermination.error("I/O error while initializing log file: " + e.getMessage());
            return;
        }

        Redirect logRedirect = Redirect.appendTo(logPath.toFile());
        ProcessBuilder pb = new ProcessBuilder(actualCommand);
        pb.redirectInput(Path.of("/dev/null").toFile());
        pb.redirectOutput(logRedirect);
        pb.redirectError(logRedirect);

        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            TaskTermination.error("I/O error while trying to run command: " + e.getMessage());
            return;
        }
        try {
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                TaskTermination.error("Timeout waiting for " + name);
                return;
            }
        } catch (InterruptedException e) {
            TaskTermination.error("Interrupted while waiting for command to finish");
        } finally {
            process.destroy();
        }

        try (BufferedWriter bw = Files.newBufferedWriter(logPath, StandardOpenOption.APPEND)) {
            bw.write("\nCommand returned exit code " + process.exitValue() + "\n");
        } catch (IOException e) {
            TaskTermination.error("I/O error while finishing log file: " + e.getMessage());
            return;
        }

        if (process.exitValue() != 0) {
            TaskTermination.fail(name + " exited with code " + process.exitValue());
            return;
        }
    }

    public void run(TaskExecutionContext context, int timeoutSeconds) throws TaskTermination {
        runImpl(context, timeoutSeconds, false);
    }

    public void runRemote(TaskExecutionContext context, int timeoutSeconds) throws TaskTermination {
        runImpl(context, timeoutSeconds, true);
    }
}
