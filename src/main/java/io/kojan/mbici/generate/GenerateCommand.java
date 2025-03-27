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
package io.kojan.mbici.generate;

import io.kojan.mbici.AbstractCommand;
import io.kojan.mbici.Main;
import io.kojan.mbici.model.Plan;
import io.kojan.mbici.model.Platform;
import io.kojan.mbici.model.Subject;
import io.kojan.workflow.model.Workflow;
import java.nio.file.Path;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/// @author Mikolaj Izdebski
@Command(
        name = "generate",
        description = "Generate Workflow from given Plan, Platform and Subject.",
        mixinStandardHelpOptions = true,
        versionProvider = Main.class)
public class GenerateCommand extends AbstractCommand {
    @Option(
            names = {"-m", "--plan"},
            required = true,
            description = "Path to a Plan in XML format.")
    private Path planPath;

    @Option(
            names = {"-p", "--platform"},
            required = true,
            description = "Path to a Platform in XML format.")
    private Path platformPath;

    @Option(
            names = {"-s", "--subject"},
            required = true,
            description = "Path to a Subject in XML format")
    private Path subjectPath;

    @Option(
            names = {"-w", "--workflow"},
            required = true,
            description = "Path where generated Workflow should be written.")
    private Path workflowPath;

    private Path testPlatformPath;

    public Path getPlanPath() {
        return planPath;
    }

    public void setPlanPath(Path planPath) {
        this.planPath = planPath;
    }

    public Path getPlatformPath() {
        return platformPath;
    }

    public void setPlatformPath(Path platformPath) {
        this.platformPath = platformPath;
    }

    public Path getSubjectPath() {
        return subjectPath;
    }

    public void setSubjectPath(Path subjectPath) {
        this.subjectPath = subjectPath;
    }

    public Path getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(Path workflowPath) {
        this.workflowPath = workflowPath;
    }

    public Path getTestPlatformPath() {
        return testPlatformPath;
    }

    public void setTestPlatformPath(Path testPlatformPath) {
        this.testPlatformPath = testPlatformPath;
    }

    @Override
    public Integer call() throws Exception {
        Plan plan = Plan.readFromXML(planPath);
        Platform platform = Platform.readFromXML(platformPath);
        Platform testPlatform =
                testPlatformPath == null ? null : Platform.readFromXML(testPlatformPath);
        Subject subject = Subject.readFromXML(subjectPath);

        WorkflowFactory wff = new WorkflowFactory();
        Workflow wfd =
                wff.createFromPlan(platform, testPlatform, plan, subject, testPlatform != null);
        wfd.writeToXML(workflowPath);

        return 0;
    }
}
