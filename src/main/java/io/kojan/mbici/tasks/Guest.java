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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Guest {

    private final Path socketPath;

    public Guest(Path workDir) throws IOException {
        socketPath = workDir.resolve("mock-chroot/root/tmp/sshd.sock");
    }

    public boolean isSshInitialized() {
        return Files.exists(socketPath);
    }

    public Path getSocketPath() throws IOException {
        return Files.readSymbolicLink(socketPath);
    }

    public void runSshServer(TaskExecutionContext context) throws TaskTermination, IOException {
        Path socketDir = Files.createTempDirectory(Path.of("/tmp"), "sock");
        List<String> script = new ArrayList<>();
        script.add("set -euxo pipefail");
        script.add("passwd -d root");
        script.add("ssh-keygen -N '' -t ed25519 -f /etc/ssh/ssh_host_ed25519_key");
        script.add("echo $$ >/tmp/sshd.pid");
        script.add("ln -s " + socketDir + "/sock /tmp/sshd.sock");
        script.add(
                "exec socat UNIX-LISTEN:"
                        + socketDir
                        + "/sock,mode=0777,fork EXEC:'/usr/sbin/sshd -i -e -oPermitRootLogin=yes -oPermitEmptyPasswords=yes -oPasswordAuthentication=yes -oUsePAM=no -oKbdInteractiveAuthentication=no -oPubkeyAuthentication=no -oGSSAPIAuthentication=no -oUseDNS=no -oX11Forwarding=no'");
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
        mock.chrootSetupCmd =
                "install bash openssh-server socat dnf util-linux-core rsync beakerlib";
        mock.bindMounts.add(socketDir);
        mock.bindMounts.add(context.getResultDir().getParent().getParent());
        mock.bindMounts.add(composePath);
        mock.run(context, "--enable-network", "--shell", String.join("\n", script));
    }

    public void runSshClient(String... args) throws InterruptedException, IOException {
        List<String> baseCmd =
                List.of(
                        "ssh",
                        "-o ProxyCommand=socat - UNIX-CONNECT:" + getSocketPath(),
                        "-o UserKnownHostsFile=/dev/null",
                        "-o StrictHostKeyChecking=no",
                        "-o PreferredAuthentications=password",
                        "-o PubkeyAuthentication=no",
                        "-o KbdInteractiveAuthentication=no",
                        "-o GSSAPIAuthentication=no",
                        "-o LogLevel=ERROR",
                        "root@dummy");
        List<String> cmd = new ArrayList<String>(baseCmd);
        cmd.addAll(Arrays.asList(args));
        new ProcessBuilder().command(cmd).inheritIO().start().waitFor();
    }
}
