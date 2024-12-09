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
package io.kojan.mbici.report;

import io.kojan.mbici.cache.ArtifactType;
import io.kojan.mbici.cache.CacheManager;
import io.kojan.mbici.model.Plan;
import io.kojan.mbici.model.Platform;
import io.kojan.mbici.model.Subject;
import io.kojan.workflow.FinishedTask;
import io.kojan.workflow.model.Artifact;
import io.kojan.workflow.model.Result;
import io.kojan.workflow.model.Task;
import io.kojan.workflow.model.TaskOutcome;
import io.kojan.workflow.model.Workflow;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @author Mikolaj Izdebski
 */
@Command(
        name = "report",
        description = "Generate a simple HTML report describing given Workflow.",
        mixinStandardHelpOptions = true)
public class ReportCommand implements Callable<Integer> {
    @Option(
            names = {"-m", "--plan"},
            required = true,
            description = "Path to a Build Plan in XML format.")
    private Path planPath;

    @Option(
            names = {"-p", "--platform"},
            required = true,
            description = "Path to a Platform in XML format.")
    private Path platformPath;

    @Option(
            names = {"-s", "--subject"},
            required = true,
            description = "Path to a Test Subject in XML format.")
    private Path subjectPath;

    @Option(
            names = {"-w", "--workflow"},
            required = true,
            description = "Path where generated Workflow should be written.")
    private Path workflowPath;

    @Option(
            names = {"-R", "--result-dir"},
            required = true,
            description = "Path to a directory with task results and artifacts.")
    private Path resultDir;

    @Option(
            names = {"-r", "--report-dir"},
            required = true,
            description = "Path to a directory where generated report should be written.")
    private Path reportDir;

    @Option(
            names = {"-t", "--tmt"},
            description = "Cenerate tmt results.yaml and include build logs.")
    private boolean full;

    @Option(
            names = {"-q", "--quiet"},
            description = "Limit the amount of logging.")
    private boolean quiet;

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

    public Path getResultDir() {
        return resultDir;
    }

    public void setResultDir(Path resultDir) {
        this.resultDir = resultDir;
    }

    public Path getReportDir() {
        return reportDir;
    }

    public void setReportDir(Path reportDir) {
        this.reportDir = reportDir;
    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    @Override
    public Integer call() throws Exception {
        Files.createDirectories(reportDir);

        CacheManager cacheManager = new CacheManager(resultDir, null, null);

        Plan plan = Plan.readFromXML(planPath);
        Platform platform = Platform.readFromXML(platformPath);
        Subject subject = Subject.readFromXML(subjectPath);
        Workflow workflow = Workflow.readFromXML(workflowPath);

        if (!quiet) {
            System.err.println("Publishing platform.xml");
        }
        platform.writeToXML(reportDir.resolve("platform.xml"));
        if (!quiet) {
            System.err.println("Publishing plan.xml");
        }
        plan.writeToXML(reportDir.resolve("plan.xml"));
        if (!quiet) {
            System.err.println("Publishing subject.xml");
        }
        subject.writeToXML(reportDir.resolve("subject.xml"));
        if (!quiet) {
            System.err.println("Publishing workflow.xml");
        }
        workflow.writeToXML(reportDir.resolve("workflow.xml"));

        Map<String, Task> tasksById = new LinkedHashMap<>();
        for (Task task : workflow.getTasks()) {
            tasksById.put(task.getId(), task);
        }

        List<FinishedTask> finishedTasks = new ArrayList<>();
        for (Result result : workflow.getResults()) {
            Task task = tasksById.get(result.getTaskId());
            FinishedTask finishedTask =
                    new FinishedTask(task, result, cacheManager.getResultDir(task, result.getId()));
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
                if (artifact.getType().equals(ArtifactType.LOG)
                        || artifact.getType().equals(ArtifactType.CONFIG)) {
                    Files.createDirectories(subDir);
                    if (!quiet) {
                        System.err.println(
                                "Publishing " + result.getTaskId() + "/" + artifact.getName());
                    }
                    Files.copy(
                            finishedTask.getArtifact(artifact), subDir.resolve(artifact.getName()));
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
