/*-
 * Copyright (c) 2021-2023 Red Hat, Inc.
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fedoraproject.mbi.ci.Command;
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
import org.fedoraproject.mbi.xml.Builder;
import org.fedoraproject.mbi.xml.Entity;

/**
 * @author Mikolaj Izdebski
 */
public class ReportCommand
    extends Command
{
    private final Path planPath;

    private final Path platformPath;

    private final Path subjectPath;

    private final Path workflowPath;

    private final Path resultDir;

    private final Path reportDir;

    private final boolean full;

    public ReportCommand( Path planPath, Path platformPath, Path subjectPath, Path workflowPath, Path resultDir,
                          Path reportDir, boolean full )
    {
        this.planPath = planPath;
        this.platformPath = platformPath;
        this.subjectPath = subjectPath;
        this.workflowPath = workflowPath;
        this.resultDir = resultDir;
        this.reportDir = reportDir;
        this.full = full;
    }

    @Override
    public void run()
        throws Exception
    {
        Files.createDirectories( reportDir );

        CacheManager cacheManager = new CacheManager( resultDir, null, null );

        Plan plan = Plan.readFromXML( planPath );
        Platform platform = Platform.readFromXML( platformPath );
        Subject subject = Subject.readFromXML( subjectPath );
        Workflow workflow = Workflow.readFromXML( workflowPath );

        System.err.println( "Publishing platform.xml" );
        platform.writeToXML( reportDir.resolve( "platform.xml" ) );
        System.err.println( "Publishing plan.xml" );
        plan.writeToXML( reportDir.resolve( "plan.xml" ) );
        System.err.println( "Publishing subject.xml" );
        subject.writeToXML( reportDir.resolve( "subject.xml" ) );
        System.err.println( "Publishing workflow.xml" );
        workflow.writeToXML( reportDir.resolve( "workflow.xml" ) );

        Map<String, Task> tasksById = new LinkedHashMap<>();
        for ( Task task : workflow.getTasks() )
        {
            tasksById.put( task.getId(), task );
        }

        List<FinishedTask> finishedTasks = new ArrayList<>();
        for ( Result result : workflow.getResults() )
        {
            Task task = tasksById.get( result.getTaskId() );
            FinishedTask finishedTask =
                new FinishedTask( task, result, cacheManager.getResultDir( task.getId(), result.getId() ) );
            finishedTasks.add( finishedTask );
        }

        for ( FinishedTask finishedTask : finishedTasks )
        {
            Result result = finishedTask.getResult();
            Path subDir = reportDir.resolve( result.getTaskId() );

            if ( full )
            {
                Files.createDirectories( subDir );
                new TmtTestoutReport( finishedTask ).publish( reportDir.resolve( result.getTaskId() ).resolve( "testout.log" ) );
            }
            else if ( result.getOutcome() == TaskOutcome.SUCCESS )
            {
                // When not in full report mode, skip publishing logs for successful tasks to conserve space.
                continue;
            }
            for ( Artifact artifact : result.getArtifacts() )
            {
                if ( artifact.getType() == ArtifactType.LOG || artifact.getType() == ArtifactType.CONFIG )
                {
                    Files.createDirectories( subDir );
                    System.err.println( "Publishing " + result.getTaskId() + "/" + artifact.getName() );
                    Files.copy( finishedTask.getResultDir().resolve( artifact.getName() ),
                                subDir.resolve( artifact.getName() ) );
                }
            }
        }

        new ResultsReport( workflow ).publish( reportDir.resolve( "result.html" ) );
        new PlatformReport( platform ).publish( reportDir.resolve( "platform.html" ) );
        new SubjectReport( subject ).publish( reportDir.resolve( "subject.html" ) );
        new PlanReport( plan ).publish( reportDir.resolve( "plan.html" ) );

        if ( full )
        {
            new TmtResultsReport( workflow ).publish( reportDir.resolve( "results.yaml" ) );
        }

        System.err.println( "REPORT COMPLETE" );
    }

    private static final class ArgsBuilder
        implements Builder<ReportCommand>
    {
        private Path planPath;

        private Path platformPath;

        private Path subjectPath;

        private Path workflowPath;

        private Path resultDir;

        private Path reportDir;

        private boolean full;

        public void setPlanPath( Path planPath )
        {
            this.planPath = planPath;
        }

        public void setPlatformPath( Path platformPath )
        {
            this.platformPath = platformPath;
        }

        public void setSubjectPath( Path subjectPath )
        {
            this.subjectPath = subjectPath;
        }

        public void setWorkflowPath( Path workflowPath )
        {
            this.workflowPath = workflowPath;
        }

        public void setResultDir( Path resultDir )
        {
            this.resultDir = resultDir;
        }

        public void setReportDir( Path reportDir )
        {
            this.reportDir = reportDir;
        }

        public void setFull( String dummy )
        {
            this.full = true;
        }

        @Override
        public ReportCommand build()
        {
            return new ReportCommand( planPath.toAbsolutePath(), platformPath.toAbsolutePath(),
                                      subjectPath.toAbsolutePath(), workflowPath.toAbsolutePath(),
                                      resultDir.toAbsolutePath(), reportDir.toAbsolutePath(), full );
        }
    }

    public static final Entity<ReportCommand, ArgsBuilder> ENTITY = new Entity<>( "report", ArgsBuilder::new );
    static
    {
        ENTITY.addAttribute( "plan", x -> null, ArgsBuilder::setPlanPath, Path::toString, Paths::get );
        ENTITY.addAttribute( "platform", x -> null, ArgsBuilder::setPlatformPath, Path::toString, Paths::get );
        ENTITY.addAttribute( "subject", x -> null, ArgsBuilder::setSubjectPath, Path::toString, Paths::get );
        ENTITY.addAttribute( "workflow", x -> null, ArgsBuilder::setWorkflowPath, Path::toString, Paths::get );
        ENTITY.addAttribute( "resultDir", x -> null, ArgsBuilder::setResultDir, Path::toString, Paths::get );
        ENTITY.addAttribute( "reportDir", x -> null, ArgsBuilder::setReportDir, Path::toString, Paths::get );
        ENTITY.addOptionalAttribute( "full", x -> null, ArgsBuilder::setFull );
    }
}
