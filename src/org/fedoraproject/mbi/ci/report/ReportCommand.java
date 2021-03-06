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
package org.fedoraproject.mbi.ci.report;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.fedoraproject.mbi.ci.Command;
import org.fedoraproject.mbi.ci.model.Plan;
import org.fedoraproject.mbi.ci.model.Platform;
import org.fedoraproject.mbi.ci.model.Subject;
import org.fedoraproject.mbi.wf.CacheManager;
import org.fedoraproject.mbi.wf.model.Artifact;
import org.fedoraproject.mbi.wf.model.ArtifactType;
import org.fedoraproject.mbi.wf.model.Result;
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

    public ReportCommand( Path planPath, Path platformPath, Path subjectPath, Path workflowPath, Path resultDir,
                          Path reportDir )
    {
        this.planPath = planPath;
        this.platformPath = platformPath;
        this.subjectPath = subjectPath;
        this.workflowPath = workflowPath;
        this.resultDir = resultDir;
        this.reportDir = reportDir;
    }

    @Override
    public void run()
        throws Exception
    {
        Files.createDirectory( reportDir );

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

        for ( Result result : workflow.getResults() )
        {
            if ( result.getOutcome() == TaskOutcome.SUCCESS )
            {
                // Skip publishing logs for successful tasks to conserve space
                continue;
            }
            for ( Artifact artifact : result.getArtifacts() )
            {
                Path subDir = reportDir.resolve( result.getTaskId() );
                if ( artifact.getType() == ArtifactType.LOG || artifact.getType() == ArtifactType.CONFIG )
                {
                    Files.createDirectories( subDir );
                    System.err.println( "Publishing " + result.getTaskId() + "/" + artifact.getName() );
                    Files.copy( cacheManager.getResultDir( result.getTaskId(),
                                                           result.getId() ).resolve( artifact.getName() ),
                                subDir.resolve( artifact.getName() ) );
                }
            }
        }

        Report resultsReport = new ResultsReport( workflow );
        resultsReport.body();
        System.err.println( "Publishing result.html" );
        resultsReport.write( reportDir.resolve( "result.html" ) );

        Report platformReport = new PlatformReport( platform );
        platformReport.body();
        System.err.println( "Publishing platform.html" );
        platformReport.write( reportDir.resolve( "platform.html" ) );

        Report subjectReport = new SubjectReport( subject );
        subjectReport.body();
        System.err.println( "Publishing subject.html" );
        subjectReport.write( reportDir.resolve( "subject.html" ) );

        Report planReport = new PlanReport( plan );
        planReport.body();
        System.err.println( "Publishing plan.html" );
        planReport.write( reportDir.resolve( "plan.html" ) );

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

        @Override
        public ReportCommand build()
        {
            return new ReportCommand( planPath.toAbsolutePath(), platformPath.toAbsolutePath(),
                                      subjectPath.toAbsolutePath(), workflowPath.toAbsolutePath(),
                                      resultDir.toAbsolutePath(), reportDir.toAbsolutePath() );
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
    }
}