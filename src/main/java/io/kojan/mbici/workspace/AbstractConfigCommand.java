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

import java.nio.file.Path;
import picocli.CommandLine.Option;

public class AbstractConfigCommand {
    @Option(
            names = {"--subject-path"},
            description = "")
    private Path subjectPath;

    @Option(
            names = {"--workflow-path"},
            description = "")
    private Path workflowPath;

    @Option(
            names = {"--plan-path"},
            description = "")
    private Path planPath;

    @Option(
            names = {"--platform-path"},
            description = "")
    private Path platformPath;

    @Option(
            names = {"--result-dir"},
            description = "")
    private Path resultDir;

    @Option(
            names = {"--cache-dir"},
            description = "")
    private Path cacheDir;

    @Option(
            names = {"--work-dir"},
            description = "")
    private Path workDir;

    @Option(
            names = {"--link-dir"},
            description = "")
    private Path linkDir;

    @Option(
            names = {"--report-dir"},
            description = "")
    private Path reportDir;

    @Option(
            names = {"--lookaside"},
            description = "")
    private String lookaside;

    @Option(
            names = {"--scm-dir"},
            description = "")
    private Path scmDir;

    @Option(
            names = {"--scm-ref"},
            description = "")
    private String scmRef;

    @Option(
            names = {"--max-checkout-tasks"},
            description = "")
    private Integer maxCheckoutTasks;

    @Option(
            names = {"--max-srpm-tasks"},
            description = "")
    private Integer maxSrpmTasks;

    @Option(
            names = {"--max-rpm-tasks"},
            description = "")
    private Integer maxRpmTasks;

    boolean updateConfig(WorkspaceConfig config) {
        boolean updated = false;
        if (subjectPath != null) {
            config.setSubjectPath(subjectPath);
            updated = true;
        }
        if (workflowPath != null) {
            config.setWorkflowPath(workflowPath);
            updated = true;
        }
        if (planPath != null) {
            config.setPlanPath(planPath);
            updated = true;
        }
        if (platformPath != null) {
            config.setPlatformPath(platformPath);
            updated = true;
        }
        if (resultDir != null) {
            config.setResultDir(resultDir);
            updated = true;
        }
        if (cacheDir != null) {
            config.setCacheDir(cacheDir);
            updated = true;
        }
        if (workDir != null) {
            config.setWorkDir(workDir);
            updated = true;
        }
        if (linkDir != null) {
            config.setLinkDir(linkDir);
            updated = true;
        }
        if (reportDir != null) {
            config.setReportDir(reportDir);
            updated = true;
        }
        if (lookaside != null) {
            config.setLookaside(lookaside);
            updated = true;
        }
        if (scmDir != null) {
            config.setScmDir(scmDir);
            updated = true;
        }
        if (scmRef != null) {
            config.setScmRef(scmRef);
            updated = true;
        }
        if (maxCheckoutTasks != null) {
            config.setMaxCheckoutTasks(maxCheckoutTasks);
            updated = true;
        }
        if (maxSrpmTasks != null) {
            config.setMaxSrpmTasks(maxSrpmTasks);
            updated = true;
        }
        if (maxRpmTasks != null) {
            config.setMaxRpmTasks(maxRpmTasks);
            updated = true;
        }
        return updated;
    }
}
