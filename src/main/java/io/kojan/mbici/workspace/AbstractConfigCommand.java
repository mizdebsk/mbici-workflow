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
import java.nio.file.Path;
import picocli.CommandLine.Option;

public abstract class AbstractConfigCommand extends AbstractCommand {
    @Option(
            names = {"--subject-path"},
            description = "An absolute path to auto-generated Subject in XML format.")
    private Path subjectPath;

    @Option(
            names = {"--workflow-path"},
            description = "An absolute path to auto-generated Workflow in XML format.")
    private Path workflowPath;

    @Option(
            names = {"--plan-path"},
            description = "An absolute path to auto-generated Plan in XML format.")
    private Path planPath;

    @Option(
            names = {"--platform-path"},
            description = "An absolute path to auto-generated Platform in XML format.")
    private Path platformPath;

    @Option(
            names = {"--result-dir"},
            description = "An absolute path to directory where build results are kept.")
    private Path resultDir;

    @Option(
            names = {"--cache-dir"},
            description = "An absolute path to directory where cached build inputs are kept.")
    private Path cacheDir;

    @Option(
            names = {"--work-dir"},
            description = "An absolute path to directory where task workding dirs are created.")
    private Path workDir;

    @Option(
            names = {"--link-dir"},
            description = "An absolute path to directory where links to task results are created.")
    private Path linkDir;

    @Option(
            names = {"--report-dir"},
            description = "An absolute path to directory where workflow HTML reports are created.")
    private Path reportDir;

    @Option(
            names = {"--compose-dir"},
            description = "An absolute path to directory where RPM compose is created.")
    private Path composeDir;

    @Option(
            names = {"--test-plan-dir"},
            description = "An absolute path to directory where tmt test plans are stored.")
    private Path testPlanDir;

    @Option(
            names = {"--test-result-dir"},
            description = "An absolute path to directory where tmt tes results are stored.")
    private Path testResultDir;

    @Option(
            names = {"--lookaside"},
            description = "The URL of upstream lookaside cache.")
    private String lookaside;

    @Option(
            names = {"--scm-dir"},
            description = "An absolute path to directory where RPM dist-git repos are stored.")
    private Path scmDir;

    @Option(
            names = {"--scm-ref"},
            description = "Git ref to use when checking out RPM dist-git repos.")
    private String scmRef;

    @Option(
            names = {"--max-checkout-tasks"},
            description = "Max number of checkout tasks running at the same time.")
    private Integer maxCheckoutTasks;

    @Option(
            names = {"--max-srpm-tasks"},
            description = "Max number of SRPM build tasks running at the same time.")
    private Integer maxSrpmTasks;

    @Option(
            names = {"--max-rpm-tasks"},
            description = "Max number of RPM build tasks running at the same time.")
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
        if (composeDir != null) {
            config.setComposeDir(composeDir);
            updated = true;
        }
        if (testPlanDir != null) {
            config.setTestPlanDir(testPlanDir);
            updated = true;
        }
        if (testResultDir != null) {
            config.setTestResultDir(testResultDir);
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
