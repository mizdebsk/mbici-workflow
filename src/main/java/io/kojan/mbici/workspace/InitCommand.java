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

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "init", description = "initialize MBI workspace", mixinStandardHelpOptions = true)
public class InitCommand extends AbstractConfigCommand {

    @Option(
            names = {"--fedora"},
            description = "assume Fedora SCM configuration")
    private boolean fedora;

    @Option(
            names = {"--centos"},
            description = "assume CentOS Stream SCM configuration")
    private boolean centos;

    @Option(
            names = {"--rhel"},
            description = "assume RHEL SCM configuration")
    private boolean rhel;

    @Override
    public Integer call() throws Exception {
        Workspace ws = Workspace.find();
        if (ws != null) {
            error("Workspace already exists at path " + ws.getWorkspaceDir());
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
        c.setComposeDir(cwd.resolve("compose"));

        if (fedora) {
            c.setLookaside("https://src.fedoraproject.org/lookaside/pkgs/rpms");
        } else if (centos) {
            c.setLookaside("https://sources.stream.centos.org/sources/rpms");
        } else if (rhel) {
            c.setLookaside("https://pkgs.devel.redhat.com/repo/pkgs");
        } else {
            c.setLookaside("");
        }
        c.setScmDir(cwd.resolve("rpms"));
        c.setScmRef("HEAD");

        c.setMaxCheckoutTasks(20);
        c.setMaxSrpmTasks(10);
        c.setMaxRpmTasks(5);

        updateConfig(c);

        Files.createDirectories(c.getCacheDir());
        Files.createDirectories(c.getResultDir());
        Files.createDirectories(c.getWorkDir());
        Files.createDirectories(c.getLinkDir());
        Files.createDirectories(c.getScmDir());

        ws = Workspace.create(cwd, c);
        ws.write();

        Path yamlPath = ws.getWorkspaceDir().resolve("mbi.yaml");
        try (Writer w = Files.newBufferedWriter(yamlPath)) {
            w.write("platform:\n");
            if (fedora) {
                // String mirror = "https://dl.fedoraproject.org/pub/fedora";
                String mirror = "https://ftp.icm.edu.pl/pub/Linux/dist/fedora";
                w.write(
                        "  Everything: "
                                + mirror
                                + "/linux/development/rawhide/Everything/x86_64/os/\n");
            } else if (centos || rhel) {
                String mirror = "https://ftp.icm.edu.pl/pub/Linux/dist/almalinux";
                w.write("  BaseOS: " + mirror + "/9/BaseOS/x86_64/os/\n");
                w.write("  AppStream: " + mirror + "/9/AppStream/x86_64/os/\n");
                w.write("  CRB: " + mirror + "/9/CRB/x86_64/os/\n");
            } else {
                w.write("  myrepo: https://...\n");
            }
            w.write("  packages:\n");
            w.write("    - rpm-build\n");
            w.write("    - glibc-minimal-langpack\n");
            w.write("\n");
            w.write("macros:\n");
            w.write("  vendor: MBI\n");
            w.write("#  my_global_macro: value\n");
            w.write("\n");
            w.write("#p0-macros:\n");
            w.write("#  _with_bootstrap: 1\n");
            w.write("\n");
            w.write("p0:\n");
            w.write("  - component1\n");
        }

        success("Initialized workspace at " + ws.getWorkspaceDir());
        return 0;
    }
}
