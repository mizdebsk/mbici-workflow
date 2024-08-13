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

import org.fedoraproject.mbi.wf.CacheManager;
import org.fedoraproject.mbi.wf.Throttle;
import org.fedoraproject.mbi.wf.WorkflowExecutor;
import org.fedoraproject.mbi.wf.model.Workflow;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @author Mikolaj Izdebski
 */
@Command( name = "run", description = "execute Workflow and update it in-place", mixinStandardHelpOptions = true )
public class RunCommand
    implements Callable<Integer>
{
    @Option( names = { "-w", "--workflow" }, description = " path to Workflow" )
    private Path workflowPath;

    @Option( names = { "-R",
        "--result-dir" }, description = "path to a directory where task results and artifacts are written" )
    private Path resultDir;

    @Option( names = { "-C",
        "--cache-dir" }, description = "path to a directory where dist-git commits and lookaside blobs are cached" )
    private Path cacheDir;

    @Option( names = { "-W",
        "--work-dir" }, description = "path to a directory under which temporary  working directories for tasks are created" )
    private Path workDir;

    @Option( names = { "--max-checkout-tasks" }, description = "limit number of parrallel git checkout tasks" )
    private Integer maxCheckoutTasks = 3;

    @Option( names = { "--max-srpm-tasks" }, description = "limit number of parrallel SRPM build tasks" )
    private Integer maxSrpmTasks = 5;

    @Option( names = { "--max-rpm-tasks" }, description = "limit number of parrallel RPM build tasks" )
    private Integer maxRpmTasks = 2;

    @Option( names = { "--max-validate-tasks" }, description = "limit number of parrallel validation tasks" )
    private Integer maxValidateTasks = 4;

    @Option( names = {
        "--kubernetes-ns" }, description = "build SRPM and RPM packages on external Kubernetes cluster instead of local machine" )
    private String kubernetesNamespace;

    @Option( names = { "-B", "--batch-mode" }, description = "Run in non-interactive mode" )
    private boolean batchMode;

    @Override
    public Integer call()
        throws Exception
    {
        Workflow wfd = Workflow.readFromXML( workflowPath );
        CacheManager cacheManager = new CacheManager( resultDir, cacheDir, workDir );
        Throttle throttle = new ThrottleImpl( maxCheckoutTasks, maxSrpmTasks, maxRpmTasks, maxValidateTasks );
        org.fedoraproject.mbi.ci.tasks.Command.kubernetesNamespace = kubernetesNamespace;
        WorkflowExecutor wfe = new WorkflowExecutor( wfd, workflowPath, cacheManager, throttle, batchMode );
        Workflow wf = wfe.execute();
        wf.writeToXML( workflowPath );
        return 0;
    }
}
