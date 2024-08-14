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
package org.fedoraproject.mbi.ci.report;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.fedoraproject.mbi.ci.model.Plan;
import org.fedoraproject.mbi.ci.model.Platform;
import org.fedoraproject.mbi.ci.model.Subject;
import org.fedoraproject.mbi.wf.CacheManager;
import org.fedoraproject.mbi.wf.FinishedTask;
import org.fedoraproject.mbi.wf.model.Artifact;
import org.fedoraproject.mbi.wf.model.ArtifactType;
import org.fedoraproject.mbi.wf.model.Result;
import org.fedoraproject.mbi.wf.model.Task;
import org.fedoraproject.mbi.wf.model.TaskOutcome;
import org.fedoraproject.mbi.wf.model.Workflow;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @author Mikolaj Izdebski
 */
@Command(name = "report", description = "generate a simple HTML report describing given Workflow", mixinStandardHelpOptions = true)
public class ReportCommand implements Callable<Integer> {
    @Option(names = {"-m", "--plan"}, required = true, description = "path to a Build Plan in XML format")
    private Path planPath;

    @Option(names = {"-p", "--platform"}, required = true, description = "path to a Platform in XML format")
    private Path platformPath;

    @Option(names = {"-s", "--subject"}, required = true, description = "path to a Test Subject in XML format")
    private Path subjectPath;

    @Option(names = {"-w",
            "--workflow"}, required = true, description = "path where generated Workflow should be written")
    private Path workflowPath;

    @Option(names = {"-R",
            "--result-dir"}, required = true, description = "path to a directory with task results and artifacts")
    private Path resultDir;

    @Option(names = {"-r",
            "--report-dir"}, required = true, description = "path to a directory where generated report should be written")
    private Path reportDir;

    @Option(names = {"-t", "--tmt"}, description = "generate tmt results.yaml and include build logs")
    private boolean full;

    @Override
    public Integer call() throws Exception {
        Files.createDirectories(reportDir);

        CacheManager cacheManager = new CacheManager(resultDir, null, null);

        Plan plan = Plan.readFromXML(planPath);
        Platform platform = Platform.readFromXML(platformPath);
        Subject subject = Subject.readFromXML(subjectPath);
        Workflow workflow = Workflow.readFromXML(workflowPath);

        System.err.println("Publishing platform.xml");
        platform.writeToXML(reportDir.resolve("platform.xml"));
        System.err.println("Publishing plan.xml");
        plan.writeToXML(reportDir.resolve("plan.xml"));
        System.err.println("Publishing subject.xml");
        subject.writeToXML(reportDir.resolve("subject.xml"));
        System.err.println("Publishing workflow.xml");
        workflow.writeToXML(reportDir.resolve("workflow.xml"));

        Map<String, Task> tasksById = new LinkedHashMap<>();
        for (Task task : workflow.getTasks()) {
            tasksById.put(task.getId(), task);
        }

        List<FinishedTask> finishedTasks = new ArrayList<>();
        for (Result result : workflow.getResults()) {
            Task task = tasksById.get(result.getTaskId());
            FinishedTask finishedTask = new FinishedTask(task, result,
                    cacheManager.getResultDir(task.getId(), result.getId()));
            finishedTasks.add(finishedTask);
        }

        for (FinishedTask finishedTask : finishedTasks) {
            Result result = finishedTask.getResult();
            Path subDir = reportDir.resolve(result.getTaskId());

            if (full) {
                Files.createDirectories(subDir);
                new TmtTestoutReport(finishedTask)
                        .publish(reportDir.resolve(result.getTaskId()).resolve("testout.log"));
            } else if (result.getOutcome() == TaskOutcome.SUCCESS) {
                // When not in full report mode, skip publishing logs for
                // successful tasks to conserve space.
                continue;
            }
            for (Artifact artifact : result.getArtifacts()) {
                if (artifact.getType() == ArtifactType.LOG || artifact.getType() == ArtifactType.CONFIG) {
                    Files.createDirectories(subDir);
                    System.err.println("Publishing " + result.getTaskId() + "/" + artifact.getName());
                    Files.copy(finishedTask.getArtifact(artifact), subDir.resolve(artifact.getName()));
                }
            }
        }

        new ResultsReport(workflow).publish(reportDir.resolve("result.html"));
        new PlatformReport(platform).publish(reportDir.resolve("platform.html"));
        new SubjectReport(subject).publish(reportDir.resolve("subject.html"));
        new PlanReport(plan).publish(reportDir.resolve("plan.html"));

        if (full) {
            new TmtResultsReport(workflow).publish(reportDir.resolve("results.yaml"));
        }

        System.err.println("REPORT COMPLETE");
        return 0;
    }
}
