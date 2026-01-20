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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/// @author Mikolaj Izdebski
class Mock {
    private static final int MOCK_TIMEOUT = 1800;

    private final Map<String, String> macros = new LinkedHashMap<>();
    String arch = Arch.getJvmArch();
    String chrootSetupCmd = "install rpm-build";
    int timeout = MOCK_TIMEOUT;
    boolean installWeakDeps = false;
    Set<Path> bindMounts = new LinkedHashSet<>();
    final Map<String, Path> repos = new LinkedHashMap<>();

    public void run(TaskExecutionContext context, String... mockArgs) throws TaskTermination {
        Path mockConfPath = context.addArtifact(ArtifactType.CONFIG, "mock.cfg");
        try (BufferedWriter bw = Files.newBufferedWriter(mockConfPath)) {
            bw.write("config_opts['basedir'] = '" + context.getWorkDir() + "'\n");
            bw.write("config_opts['cache_topdir'] = '" + context.getWorkDir() + "'\n");
            bw.write("");
            bw.write("config_opts['rpmbuild_networking'] = False\n");
            bw.write("config_opts['use_host_resolv'] = False\n");
            bw.write("config_opts['isolation'] = 'chroot'\n");
            bw.write("config_opts['use_bootstrap'] = False\n");
            bw.write("config_opts['plugin_conf']['yum_cache_enable'] = False\n");
            bw.write("config_opts['plugin_conf']['root_cache_enable'] = False\n");
            bw.write("config_opts['plugin_conf']['package_state_enable'] = False\n");
            bw.write("config_opts['plugin_conf']['tmpfs_enable'] = False\n");
            bw.write("config_opts['plugin_conf']['tmpfs_opts']['required_ram_mb'] = 2048\n");
            bw.write("config_opts['plugin_conf']['tmpfs_opts']['max_fs_size'] = '4g'\n");
            // bw.write( "config_opts['nosync'] = True\n" );
            // bw.write( "config_opts['nosync_force'] = True\n" );
            bw.write("config_opts['root'] = 'mock-chroot'\n");
            bw.write("config_opts['target_arch'] = '" + arch + "'\n");
            bw.write("config_opts['chroot_setup_cmd'] = '" + chrootSetupCmd + "'\n");
            bw.write("\n");
            for (Path bindMount : bindMounts) {
                bw.write(
                        "config_opts['plugin_conf']['bind_mount_opts']['dirs'].append(('"
                                + bindMount
                                + "', '"
                                + bindMount
                                + "'))\n");
            }
            bw.write("config_opts['macros']['%_source_payload'] = 'w.ufdio'\n");
            bw.write("config_opts['macros']['%_binary_payload'] = 'w.ufdio'\n");
            for (var macro : macros.entrySet()) {
                bw.write(
                        "config_opts['macros']['%"
                                + macro.getKey()
                                + "'] = '"
                                + macro.getValue()
                                + "'\n");
            }
            bw.write("\n");
            bw.write("config_opts['yum.conf'] = \"\"\"\n");
            bw.write("[main]\n");
            bw.write("keepcache=0\n");
            bw.write("debuglevel=1\n");
            bw.write("reposdir=/dev/null\n");
            bw.write("gpgcheck=0\n");
            bw.write("assumeyes=1\n");
            bw.write("install_weak_deps=" + (installWeakDeps ? 1 : 0) + "\n");
            bw.write("metadata_expire=-1\n");
            bw.write("best=1\n");
            bw.write("protected_packages=rpm-build\n");

            Map<String, Path> repos = new LinkedHashMap<>(this.repos);
            for (Path repoPath : context.getDependencyArtifacts(ArtifactType.REPO)) {
                // FIXME find a better way to determine repo name
                String repoName = repoPath.getParent().getParent().getFileName().toString();
                repos.put(repoName, repoPath);
            }
            int priority = 0;
            for (var entry : repos.entrySet()) {
                String repoName = entry.getKey();
                Path repoPath = entry.getValue();
                bw.write("\n");
                bw.write("[" + repoName + "]\n");
                bw.write("name=" + repoName + "\n");
                bw.write("baseurl=" + repoPath + "\n");
                bw.write("priority=" + ++priority + "\n");
                bw.write("module_hotfixes=1\n");
            }

            bw.write("\"\"\"\n");
        } catch (IOException e) {
            TaskTermination.error("I/O error when writing mock config: " + e.getMessage());
        }

        Command mock = new Command("mock");
        mock.addArg("--enable-plugin", "tmpfs");
        mock.addArg("-r", mockConfPath.toString());
        mock.addArg("--resultdir", context.getResultDir().toString());
        mock.addArg(mockArgs);
        mock.runRemote(context, timeout);

        for (String logName : Arrays.asList("build.log", "root.log", "hw_info.log", "state.log")) {
            if (Files.isRegularFile(context.getResultDir().resolve(logName))) {
                context.addArtifact(ArtifactType.LOG, logName);
            }
        }
    }

    public void addMacro(String name, String value) {
        this.macros.put(name, value);
    }
}
