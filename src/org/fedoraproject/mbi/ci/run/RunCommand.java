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

    public RunCommand( Path workflowPath, Path resultDir, Path cacheDir, Path workDir )
    {
        this.workflowPath = workflowPath;
        this.resultDir = resultDir;
        this.cacheDir = cacheDir;
        this.workDir = workDir;
    }

    @Override
    public void run()
        throws Exception
    {
        Workflow wfd = Workflow.readFromXML( workflowPath );
        CacheManager cacheManager = new CacheManager( resultDir, cacheDir, workDir );
        WorkflowExecutor wfe = new WorkflowExecutor( wfd, workflowPath, cacheManager );
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

        @Override
        public RunCommand build()
        {
            return new RunCommand( workflowPath, resultDir, cacheDir, workDir );
        }
    }

    public static final Entity<RunCommand, ArgsBuilder> ENTITY = new Entity<>( "run", ArgsBuilder::new );
    static
    {
        ENTITY.addAttribute( "workflow", x -> null, ArgsBuilder::setWorkflowPath, Path::toString, Paths::get );
        ENTITY.addAttribute( "resultDir", x -> null, ArgsBuilder::setResultDir, Path::toString, Paths::get );
        ENTITY.addAttribute( "cacheDir", x -> null, ArgsBuilder::setCacheDir, Path::toString, Paths::get );
        ENTITY.addAttribute( "workDir", x -> null, ArgsBuilder::setWorkDir, Path::toString, Paths::get );
    }
}