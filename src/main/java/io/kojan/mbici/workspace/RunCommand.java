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
import io.kojan.mbici.execute.LocalExecuteCommand;
import io.kojan.mbici.generate.GenerateCommand;
import io.kojan.mbici.report.ReportCommand;
import io.kojan.mbici.subject.LocalSubjectCommand;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "run",
        description = "Generate and execute MBI workflow from YAML definition.",
        mixinStandardHelpOptions = true,
        versionProvider = Main.class)
public class RunCommand extends AbstractCommand {

    @Option(
            names = {"-B", "--batch-mode"},
            description = "Run in non-interactive mode.")
    protected boolean batchMode;

    private static void deleteDir(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                for (Path child : ds) {
                    deleteDir(child);
                }
            }
        }
        Files.delete(path);
    }

    private static void copyDir(Path src, Path dest) throws IOException {
        Files.copy(src, dest);
        if (Files.isDirectory(src)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(src)) {
                for (Path child : ds) {
                    copyDir(child, dest.resolve(child.getFileName()));
                }
            }
        }
    }

    @Override
    public Integer call() throws Exception {

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
        generate.setSubjectPath(c.getSubjectPath());
        generate.setWorkflowPath(c.getWorkflowPath());

        info("Running generate command...");
        ret = generate.call();
        if (ret != 0) {
            error("The generate command failed");
            return ret;
        }

        LocalExecuteCommand execute = new LocalExecuteCommand();
        execute.setWorkflowPath(c.getWorkflowPath());
        execute.setResultDir(c.getResultDir());
        execute.setCacheDir(c.getCacheDir());
        execute.setWorkDir(c.getWorkDir());
        execute.setLinkerDir(c.getLinkDir());
        execute.setMaxCheckoutTasks(c.getMaxCheckoutTasks());
        execute.setMaxSrpmTasks(c.getMaxSrpmTasks());
        execute.setMaxRpmTasks(c.getMaxRpmTasks());
        execute.setBatchMode(batchMode);

        info("Running execute command...");
        ret = execute.call();
        if (ret != 0) {
            error("The execute command failed");
            return ret;
        }

        Files.createDirectories(c.getReportDir());
        deleteDir(c.getReportDir());

        ReportCommand report = new ReportCommand();
        report.setPlanPath(c.getPlanPath());
        report.setPlatformPath(c.getPlatformPath());
        report.setSubjectPath(c.getSubjectPath());
        report.setWorkflowPath(c.getWorkflowPath());
        report.setResultDir(c.getResultDir());
        report.setReportDir(c.getReportDir());
        report.setFull(true);
        report.setQuiet(true);

        Files.createDirectories(c.getComposeDir());
        deleteDir(c.getComposeDir());
        Path composeRepoDir = c.getLinkDir().resolve("compose").resolve("repo");
        if (Files.isDirectory(composeRepoDir)) {
            copyDir(composeRepoDir, c.getComposeDir());
        }

        info("Running report command...");
        ret = report.call();
        if (ret != 0) {
            error("The report command failed");
            return ret;
        }

        return 0;
    }
}
