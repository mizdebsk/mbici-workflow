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
package io.kojan.mbici.execute;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import io.kojan.workflow.CacheManager;
import io.kojan.workflow.TaskHandlerFactory;
import io.kojan.workflow.Throttle;
import io.kojan.workflow.WorkflowExecutor;
import io.kojan.workflow.model.Workflow;
import picocli.CommandLine.Option;

/**
 * @author Mikolaj Izdebski
 */
abstract class AbstractExecuteCommand implements Callable<Integer> {
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

    @Option(names = {"-B", "--batch-mode"}, description = "Run in non-interactive mode")
    protected boolean batchMode;

    @Option(names = {"-h", "--webhook-url"}, description = "Notify webhook about workflow state changes")
    protected String webhookUrl;

    @Option(names = {"-t", "--webhook-token"}, description = "Bearer token to use for webhook authorization")
    protected String webhookToken;

    @Override
    public Integer call() throws Exception {
        Workflow wfd = Workflow.readFromXML(workflowPath);
        TaskHandlerFactory handlerFactory = new TaskHandlerFactoryImpl();
        CacheManager cacheManager = new CacheManager(resultDir, cacheDir, workDir);
        Throttle throttle = new ThrottleImpl(maxCheckoutTasks, maxSrpmTasks, maxRpmTasks);
        WorkflowExecutor wfe = new WorkflowExecutor(wfd, workflowPath, handlerFactory, cacheManager, throttle,
                batchMode);
        Dumper dumper = new Dumper(workflowPath);
        dumper.setDaemon(true);
        dumper.start();
        wfe.addExecutionListener(dumper);
        if (webhookUrl != null) {
            WebHookDumper webhook = new WebHookDumper(webhookUrl, webhookToken);
            webhook.setDaemon(true);
            webhook.start();
            wfe.addExecutionListener(webhook);
        }
        Workflow wf = wfe.execute();
        wf.writeToXML(workflowPath);
        return 0;
    }
}
