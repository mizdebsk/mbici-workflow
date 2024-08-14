/*-
 * Copyright (c) 2021-2024 Red Hat, Inc.
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

import java.nio.file.Path;
import java.util.concurrent.Callable;

import io.kojan.mbici.model.Plan;
import io.kojan.mbici.model.Platform;
import io.kojan.mbici.model.Subject;
import io.kojan.workflow.model.Workflow;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @author Mikolaj Izdebski
 */
@Command(name = "generate", description = "generate Workflow from given Build Plan, Platform and Test Subject", mixinStandardHelpOptions = true)
public class GenerateCommand implements Callable<Integer> {
    @Option(names = {"-m", "--plan"}, required = true, description = "path to a Build Plan in XML format")
    private Path planPath;

    @Option(names = {"-p", "--platform"}, required = true, description = "path to a Platform in XML format")
    private Path platformPath;

    @Option(names = {"-s", "--subject"}, required = true, description = "path to a Test Subject in XML format")
    private Path subjectPath;

    @Option(names = {"-w",
            "--workflow"}, required = true, description = "path where generated Workflow should be written")
    private Path workflowPath;

    @Option(names = {"-t", "--validate"}, description = "incude validation tasks in generated Workflow")
    private boolean validate;

    @Override
    public Integer call() throws Exception {
        Plan plan = Plan.readFromXML(planPath);
        Platform platform = Platform.readFromXML(platformPath);
        Subject subject = Subject.readFromXML(subjectPath);

        WorkflowFactory wff = new WorkflowFactory();
        Workflow wfd = wff.createFromPlan(platform, plan, subject, validate);
        wfd.writeToXML(workflowPath);

        return 0;
    }
}