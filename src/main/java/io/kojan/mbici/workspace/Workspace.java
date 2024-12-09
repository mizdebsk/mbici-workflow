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

import io.kojan.xml.Attribute;
import io.kojan.xml.Entity;
import io.kojan.xml.XMLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Workspace {
    private final Path workspaceDir;
    private final WorkspaceConfig config;

    public Path getWorkspaceDir() {
        return workspaceDir;
    }

    public WorkspaceConfig getConfig() {
        return config;
    }

    Workspace(Path workspaceDir, WorkspaceConfig config) {
        this.workspaceDir = workspaceDir;
        this.config = config;
    }

    public static Workspace read(Path workspaceDir) throws IOException, XMLException {
        Path configPath = workspaceDir.resolve(".mbi").resolve("workspace.xml");
        WorkspaceConfig config = confEntity.readFromXML(configPath);
        return new Workspace(workspaceDir, config);
    }

    public void write() throws IOException, XMLException {
        Path configPath = workspaceDir.resolve(".mbi").resolve("workspace.xml");
        confEntity.writeToXML(configPath, config);
    }

    public static Workspace create(Path p, WorkspaceConfig config) {
        return new Workspace(p, config);
    }

    public static Workspace find() throws IOException, XMLException {
        return find(Path.of(".").toAbsolutePath().getParent());
    }

    public static Workspace findOrAbort() throws IOException, XMLException {
        Workspace ws = find();
        if (ws == null) {
            throw new RuntimeException("This must be ran in a workspace");
        }
        return ws;
    }

    public static Workspace find(Path pp) throws IOException, XMLException {
        for (Path p = pp; p != null; p = p.getParent()) {
            if (Files.isDirectory(p.resolve(".mbi"))) {
                return read(p);
            }
        }
        return null;
    }

    private static final Entity<WorkspaceConfig, WorkspaceConfig> confEntity =
            Entity.ofMutable(
                    "workspace",
                    WorkspaceConfig::new,
                    Attribute.of(
                            "subjectPath",
                            WorkspaceConfig::getSubjectPath,
                            WorkspaceConfig::setSubjectPath,
                            Path::toString,
                            Path::of),
                    Attribute.of(
                            "workflowPath",
                            WorkspaceConfig::getWorkflowPath,
                            WorkspaceConfig::setWorkflowPath,
                            Path::toString,
                            Path::of),
                    Attribute.of(
                            "planPath",
                            WorkspaceConfig::getPlanPath,
                            WorkspaceConfig::setPlanPath,
                            Path::toString,
                            Path::of),
                    Attribute.of(
                            "platformPath",
                            WorkspaceConfig::getPlatformPath,
                            WorkspaceConfig::setPlatformPath,
                            Path::toString,
                            Path::of),
                    Attribute.of(
                            "resultDir",
                            WorkspaceConfig::getResultDir,
                            WorkspaceConfig::setResultDir,
                            Path::toString,
                            Path::of),
                    Attribute.of(
                            "cacheDir",
                            WorkspaceConfig::getCacheDir,
                            WorkspaceConfig::setCacheDir,
                            Path::toString,
                            Path::of),
                    Attribute.of(
                            "workDir",
                            WorkspaceConfig::getWorkDir,
                            WorkspaceConfig::setWorkDir,
                            Path::toString,
                            Path::of),
                    Attribute.of(
                            "linkDir",
                            WorkspaceConfig::getLinkDir,
                            WorkspaceConfig::setLinkDir,
                            Path::toString,
                            Path::of),
                    Attribute.of(
                            "reportDir",
                            WorkspaceConfig::getReportDir,
                            WorkspaceConfig::setReportDir,
                            Path::toString,
                            Path::of),
                    Attribute.of(
                            "composeDir",
                            WorkspaceConfig::getComposeDir,
                            WorkspaceConfig::setComposeDir,
                            Path::toString,
                            Path::of),
                    Attribute.of(
                            "testResultDir",
                            WorkspaceConfig::getTestResultDir,
                            WorkspaceConfig::setTestResultDir,
                            Path::toString,
                            Path::of),
                    Attribute.of(
                            "maxCheckoutTasks",
                            WorkspaceConfig::getMaxCheckoutTasks,
                            WorkspaceConfig::setMaxCheckoutTasks,
                            Number::toString,
                            Integer::parseInt),
                    Attribute.of(
                            "lookaside",
                            WorkspaceConfig::getLookaside,
                            WorkspaceConfig::setLookaside),
                    Attribute.of(
                            "scmDir",
                            WorkspaceConfig::getScmDir,
                            WorkspaceConfig::setScmDir,
                            Path::toString,
                            Path::of),
                    Attribute.of("scmRef", WorkspaceConfig::getScmRef, WorkspaceConfig::setScmRef),
                    Attribute.of(
                            "testPlanDir",
                            WorkspaceConfig::getTestPlanDir,
                            WorkspaceConfig::setTestPlanDir,
                            Path::toString,
                            Path::of),
                    Attribute.of(
                            "maxSrpmTasks",
                            WorkspaceConfig::getMaxSrpmTasks,
                            WorkspaceConfig::setMaxSrpmTasks,
                            Number::toString,
                            Integer::parseInt),
                    Attribute.of(
                            "maxRpmTasks",
                            WorkspaceConfig::getMaxRpmTasks,
                            WorkspaceConfig::setMaxRpmTasks,
                            Number::toString,
                            Integer::parseInt));
}
