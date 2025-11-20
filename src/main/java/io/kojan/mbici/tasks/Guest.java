/*-
 * Copyright (c) 2025 Red Hat, Inc.
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

import io.kojan.workflow.TaskExecutionContext;
import io.kojan.workflow.TaskTermination;
import io.kojan.workflow.model.Parameter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Guest {

    public static final String SSH_HOST = "localhost";
    public static final String SSH_PORT = "17593";
    public static final String SSH_USER = "root";
    public static final String SSH_PUB_KEY = System.getProperty("user.home") + "/.ssh/id_rsa.pub";
    public static final String SSH_PRIV_KEY = System.getProperty("user.home") + "/.ssh/id_rsa";

    private final Path pidFilePathChrooted;
    private final Path pidFilePath;

    public Guest(Path workDir) {
        pidFilePathChrooted = Path.of("/tmp/sshd.pid");
        pidFilePath =
                workDir.resolve("mock-chroot")
                        .resolve("root")
                        .resolve(Path.of("/").relativize(pidFilePathChrooted));
    }

    public boolean isSshInitialized() {
        return Files.isRegularFile(pidFilePath);
    }

    public void runSshServer(TaskExecutionContext context) throws TaskTermination, IOException {
        List<String> script = new ArrayList<>();
        script.add("set -euxo pipefail");
        script.add("ssh-keygen -A");
        script.add("chmod 700 /root/.ssh");
        script.add("chmod 600 /root/.ssh/authorized_keys");
        script.add("echo $$ >" + pidFilePathChrooted);
        script.add("exec /usr/sbin/sshd -D -p " + SSH_PORT);
        List<Parameter> parameters = context.getTask().getParameters();
        if (parameters.size() != 1 || !parameters.getFirst().getName().equals("compose")) {
            throw new IllegalArgumentException(
                    getClass().getName() + " takes exactly one parameter called \"compose\"");
        }
        Path composePath = Path.of(parameters.getFirst().getValue());
        Mock mock = new Mock();
        mock.repos.put("compose", composePath);
        mock.timeout = Integer.MAX_VALUE;
        mock.installWeakDeps = true;
        mock.chrootSetupCmd = "install bash openssh-server dnf util-linux-core rsync beakerlib";
        mock.authorizedSshKeys.add(
                Files.readString(Path.of(SSH_PUB_KEY), StandardCharsets.UTF_8).trim());
        mock.bindMounts.add(context.getResultDir().getParent().getParent());
        mock.bindMounts.add(composePath);
        mock.run(context, "--enable-network", "--shell", String.join("\n", script));
    }

    public void runSshClient(String... args) throws InterruptedException, IOException {
        List<String> baseCmd =
                List.of(
                        "ssh",
                        "-oForwardX11=no",
                        "-oStrictHostKeyChecking=no",
                        "-oUserKnownHostsFile=/dev/null",
                        "-oConnectionAttempts=5",
                        "-oConnectTimeout=60",
                        "-oServerAliveInterval=5",
                        "-oServerAliveCountMax=60",
                        "-oIdentitiesOnly=yes",
                        "-oPasswordAuthentication=no",
                        "-oGSSAPIAuthentication=no",
                        "-oLogLevel=ERROR",
                        "-i",
                        SSH_PRIV_KEY,
                        "-p",
                        SSH_PORT,
                        SSH_USER + "@" + SSH_HOST);
        List<String> cmd = new ArrayList<String>(baseCmd);
        cmd.addAll(Arrays.asList(args));
        new ProcessBuilder().command(cmd).inheritIO().start().waitFor();
    }
}
