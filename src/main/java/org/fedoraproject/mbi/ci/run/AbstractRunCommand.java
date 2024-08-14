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
package org.fedoraproject.mbi.ci.run;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.fedoraproject.mbi.ci.tasks.CheckoutTaskHandler;
import org.fedoraproject.mbi.ci.tasks.GatherTaskHandler;
import org.fedoraproject.mbi.ci.tasks.RepoTaskHandler;
import org.fedoraproject.mbi.ci.tasks.RpmTaskHandler;
import org.fedoraproject.mbi.ci.tasks.SrpmTaskHandler;
import org.fedoraproject.mbi.ci.tasks.ValidateTaskHandler;

import io.kojan.workflow.CacheManager;
import io.kojan.workflow.TaskHandlerFactory;
import io.kojan.workflow.Throttle;
import io.kojan.workflow.WorkflowExecutor;
import io.kojan.workflow.model.Workflow;
import picocli.CommandLine.Option;

/**
 * @author Mikolaj Izdebski
 */
abstract class AbstractRunCommand implements Callable<Integer> {
    @Option(names = {"-w", "--workflow"}, required = true, description = " path to Workflow")
    protected Path workflowPath;

    @Option(names = {"-R",
            "--result-dir"}, required = true, description = "path to a directory where task results and artifacts are written")
    protected Path resultDir;

    @Option(names = {"-C",
            "--cache-dir"}, required = true, description = "path to a directory where dist-git commits and lookaside blobs are cached")
    protected Path cacheDir;

    @Option(names = {"-W",
            "--work-dir"}, required = true, description = "path to a directory under which temporary working directories for tasks are created")
    protected Path workDir;

    @Option(names = {"--max-checkout-tasks"}, description = "limit number of parrallel git checkout tasks")
    protected Integer maxCheckoutTasks = 3;

    @Option(names = {"--max-srpm-tasks"}, description = "limit number of parrallel SRPM build tasks")
    protected Integer maxSrpmTasks = 5;

    @Option(names = {"--max-rpm-tasks"}, description = "limit number of parrallel RPM build tasks")
    protected Integer maxRpmTasks = 2;

    @Option(names = {"--max-validate-tasks"}, description = "limit number of parrallel validation tasks")
    protected Integer maxValidateTasks = 4;

    @Option(names = {"-B", "--batch-mode"}, description = "Run in non-interactive mode")
    protected boolean batchMode;

    @Override
    public Integer call() throws Exception {
        Workflow wfd = Workflow.readFromXML(workflowPath);
        TaskHandlerFactory handlerFactory = new TaskHandlerFactory();
        handlerFactory.registerHandler(CheckoutTaskHandler.class, CheckoutTaskHandler::new);
        handlerFactory.registerHandler(GatherTaskHandler.class, GatherTaskHandler::new);
        handlerFactory.registerHandler(RepoTaskHandler.class, RepoTaskHandler::new);
        handlerFactory.registerHandler(RpmTaskHandler.class, RpmTaskHandler::new);
        handlerFactory.registerHandler(SrpmTaskHandler.class, SrpmTaskHandler::new);
        handlerFactory.registerHandler(ValidateTaskHandler.class, ValidateTaskHandler::new);
        CacheManager cacheManager = new CacheManager(resultDir, cacheDir, workDir);
        Throttle throttle = new ThrottleImpl(maxCheckoutTasks, maxSrpmTasks, maxRpmTasks, maxValidateTasks);
        WorkflowExecutor wfe = new WorkflowExecutor(wfd, workflowPath, handlerFactory, cacheManager, throttle,
                batchMode);
        Workflow wf = wfe.execute();
        wf.writeToXML(workflowPath);
        return 0;
    }
}
