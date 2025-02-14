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
package io.kojan.mbici.execute;

import io.kojan.mbici.AbstractCommand;
import io.kojan.mbici.cache.CacheManager;
import io.kojan.workflow.TaskHandlerFactory;
import io.kojan.workflow.TaskThrottle;
import io.kojan.workflow.WorkflowExecutor;
import io.kojan.workflow.model.Workflow;
import java.nio.file.Path;
import picocli.CommandLine.Option;

/**
 * @author Mikolaj Izdebski
 */
public abstract class AbstractExecuteCommand extends AbstractCommand {
    @Option(
            names = {"-w", "--workflow"},
            required = true,
            description = "An absolute path to Workflow in XML format.")
    protected Path workflowPath;

    @Option(
            names = {"-R", "--result-dir"},
            required = true,
            description = "An absolute path to directory where build results are kept.")
    protected Path resultDir;

    @Option(
            names = {"-C", "--cache-dir"},
            required = true,
            description = "An absolute path to directory where cached build inputs are kept.")
    protected Path cacheDir;

    @Option(
            names = {"-W", "--work-dir"},
            required = true,
            description = "An absolute path to directory where task workding dirs are created.")
    protected Path workDir;

    @Option(
            names = {"-L", "--link-dir"},
            description = "An absolute path to directory where links to task results are created.")
    protected Path linkerDir;

    @Option(
            names = {"--max-checkout-tasks"},
            description = "Max number of checkout tasks running at the same time.")
    protected Integer maxCheckoutTasks = 3;

    @Option(
            names = {"--max-srpm-tasks"},
            description = "Max number of SRPM build tasks running at the same time.")
    protected Integer maxSrpmTasks = 5;

    @Option(
            names = {"--max-rpm-tasks"},
            description = "Max number of RPM build tasks running at the same time.")
    protected Integer maxRpmTasks = 2;

    @Option(
            names = {"-B", "--batch-mode"},
            description = "Run in non-interactive mode.")
    protected boolean batchMode;

    @Option(
            names = {"-h", "--webhook-url"},
            description = "Notify HTTP webhook about Workflow state changes.")
    protected String webhookUrl;

    @Option(
            names = {"-t", "--webhook-token"},
            description = "Bearer token to use for webhook authorization.")
    protected String webhookToken;

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

    public Path getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(Path cacheDir) {
        this.cacheDir = cacheDir;
    }

    public Path getWorkDir() {
        return workDir;
    }

    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    public Path getLinkerDir() {
        return linkerDir;
    }

    public void setLinkerDir(Path linkerDir) {
        this.linkerDir = linkerDir;
    }

    public Integer getMaxCheckoutTasks() {
        return maxCheckoutTasks;
    }

    public void setMaxCheckoutTasks(Integer maxCheckoutTasks) {
        this.maxCheckoutTasks = maxCheckoutTasks;
    }

    public Integer getMaxSrpmTasks() {
        return maxSrpmTasks;
    }

    public void setMaxSrpmTasks(Integer maxSrpmTasks) {
        this.maxSrpmTasks = maxSrpmTasks;
    }

    public Integer getMaxRpmTasks() {
        return maxRpmTasks;
    }

    public void setMaxRpmTasks(Integer maxRpmTasks) {
        this.maxRpmTasks = maxRpmTasks;
    }

    public boolean isBatchMode() {
        return batchMode;
    }

    public void setBatchMode(boolean batchMode) {
        this.batchMode = batchMode;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getWebhookToken() {
        return webhookToken;
    }

    public void setWebhookToken(String webhookToken) {
        this.webhookToken = webhookToken;
    }

    private CacheManager cacheManager;

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    protected void initialize() {}

    @Override
    public Integer call() throws Exception {
        Workflow wfd = Workflow.readFromXML(workflowPath);
        cacheManager = new CacheManager(resultDir, cacheDir, workDir);
        initialize();
        TaskHandlerFactory handlerFactory = new TaskHandlerFactoryImpl(cacheManager);
        TaskThrottle throttle = new ThrottleImpl(maxCheckoutTasks, maxSrpmTasks, maxRpmTasks);
        WorkflowExecutor wfe =
                new WorkflowExecutor(wfd, handlerFactory, cacheManager, throttle, batchMode);
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
        if (linkerDir != null) {
            Linker linker = new Linker(linkerDir);
            wfe.addExecutionListener(linker);
        }
        Workflow wf = wfe.execute();
        wf.writeToXML(workflowPath);
        return 0;
    }
}
