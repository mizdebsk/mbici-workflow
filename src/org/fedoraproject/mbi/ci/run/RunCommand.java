/*-
 * Copyright (c) 2021 Red Hat, Inc.
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
import java.nio.file.Paths;

import org.fedoraproject.mbi.ci.Command;
import org.fedoraproject.mbi.wf.CacheManager;
import org.fedoraproject.mbi.wf.Throttle;
import org.fedoraproject.mbi.wf.WorkflowExecutor;
import org.fedoraproject.mbi.wf.model.Workflow;
import org.fedoraproject.mbi.xml.Builder;
import org.fedoraproject.mbi.xml.Entity;

/**
 * @author Mikolaj Izdebski
 */
public class RunCommand
    extends Command
{
    private final Path workflowPath;

    private final Path resultDir;

    private final Path cacheDir;

    private final Path workDir;

    private final int maxCheckoutTasks;

    private final int maxSrpmTasks;

    private final int maxRpmTasks;

    private final int maxValidateTasks;

    private final String kubernetesNamespace;

    private final boolean batchMode;

    public RunCommand( Path workflowPath, Path resultDir, Path cacheDir, Path workDir, int maxCheckoutTasks,
                       int maxSrpmTasks, int maxRpmTasks, int maxValidateTasks, String kubernetesNamespace,
                       boolean batchMode )
    {
        this.workflowPath = workflowPath;
        this.resultDir = resultDir;
        this.cacheDir = cacheDir;
        this.workDir = workDir;
        this.maxCheckoutTasks = maxCheckoutTasks;
        this.maxSrpmTasks = maxSrpmTasks;
        this.maxRpmTasks = maxRpmTasks;
        this.maxValidateTasks = maxValidateTasks;
        this.kubernetesNamespace = kubernetesNamespace;
        this.batchMode = batchMode;
    }

    @Override
    public void run()
        throws Exception
    {
        Workflow wfd = Workflow.readFromXML( workflowPath );
        CacheManager cacheManager = new CacheManager( resultDir, cacheDir, workDir );
        Throttle throttle = new ThrottleImpl( maxCheckoutTasks, maxSrpmTasks, maxRpmTasks, maxValidateTasks );
        org.fedoraproject.mbi.wf.handler.Command.kubernetesNamespace = kubernetesNamespace;
        WorkflowExecutor wfe = new WorkflowExecutor( wfd, workflowPath, cacheManager, throttle, batchMode );
        Workflow wf = wfe.execute();
        wf.writeToXML( workflowPath );
    }

    private static class ArgsBuilder
        implements Builder<RunCommand>
    {
        private Path workflowPath;

        private Path resultDir;

        private Path cacheDir;

        private Path workDir;

        private Integer maxCheckoutTasks = 3;

        private Integer maxSrpmTasks = 5;

        private Integer maxRpmTasks = 2;

        private Integer maxValidateTasks = 4;

        private String kubernetesNamespace;

        private Boolean batchMode = false;

        public void setWorkflowPath( Path workflowPath )
        {
            this.workflowPath = workflowPath;
        }

        public void setResultDir( Path resultDir )
        {
            this.resultDir = resultDir;
        }

        public void setCacheDir( Path cacheDir )
        {
            this.cacheDir = cacheDir;
        }

        public void setWorkDir( Path workDir )
        {
            this.workDir = workDir;
        }

        public void setMaxCheckoutTasks( Integer maxCheckoutTasks )
        {
            this.maxCheckoutTasks = maxCheckoutTasks;
        }

        public void setMaxSrpmTasks( Integer maxSrpmTasks )
        {
            this.maxSrpmTasks = maxSrpmTasks;
        }

        public void setMaxRpmTasks( Integer maxRpmTasks )
        {
            this.maxRpmTasks = maxRpmTasks;
        }

        public void setMaxValidateTasks( Integer maxValidateTasks )
        {
            this.maxValidateTasks = maxValidateTasks;
        }

        public void setKubernetes( String kubernetesNamespace )
        {
            this.kubernetesNamespace = kubernetesNamespace;
        }

        public void setBatchMode( String dummy )
        {
            this.batchMode = true;
        }

        @Override
        public RunCommand build()
        {
            return new RunCommand( workflowPath.toAbsolutePath(), resultDir.toAbsolutePath(), cacheDir.toAbsolutePath(),
                                   workDir.toAbsolutePath(), maxCheckoutTasks, maxSrpmTasks, maxRpmTasks,
                                   maxValidateTasks, kubernetesNamespace, batchMode );
        }
    }

    public static final Entity<RunCommand, ArgsBuilder> ENTITY = new Entity<>( "run", ArgsBuilder::new );
    static
    {
        ENTITY.addAttribute( "workflow", x -> null, ArgsBuilder::setWorkflowPath, Path::toString, Paths::get );
        ENTITY.addAttribute( "resultDir", x -> null, ArgsBuilder::setResultDir, Path::toString, Paths::get );
        ENTITY.addAttribute( "cacheDir", x -> null, ArgsBuilder::setCacheDir, Path::toString, Paths::get );
        ENTITY.addAttribute( "workDir", x -> null, ArgsBuilder::setWorkDir, Path::toString, Paths::get );
        ENTITY.addOptionalAttribute( "maxCheckoutTasks", x -> null, ArgsBuilder::setMaxCheckoutTasks, Number::toString,
                                     Integer::parseInt );
        ENTITY.addOptionalAttribute( "maxSrpmTasks", x -> null, ArgsBuilder::setMaxSrpmTasks, Number::toString,
                                     Integer::parseInt );
        ENTITY.addOptionalAttribute( "maxRpmTasks", x -> null, ArgsBuilder::setMaxRpmTasks, Number::toString,
                                     Integer::parseInt );
        ENTITY.addOptionalAttribute( "maxValidateTasks", x -> null, ArgsBuilder::setMaxValidateTasks, Number::toString,
                                     Integer::parseInt );
        ENTITY.addOptionalAttribute( "kubernetesNamespace", x -> null, ArgsBuilder::setKubernetes );
        ENTITY.addOptionalAttribute( "batch", x -> null, ArgsBuilder::setBatchMode );
    }
}
