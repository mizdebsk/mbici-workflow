/*-
 * Copyright (c) 2022 Red Hat, Inc.
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

import java.util.List;
import java.util.stream.Collectors;

import org.fedoraproject.mbi.wf.model.Artifact;
import org.fedoraproject.mbi.wf.model.ArtifactType;
import org.fedoraproject.mbi.wf.model.Result;
import org.fedoraproject.mbi.wf.model.TaskOutcome;
import org.fedoraproject.mbi.wf.model.Workflow;

/**
 * @author Mikolaj Izdebski
 */
public class ResultsReport
    extends Report
{
    private final Workflow workflow;

    public ResultsReport( Workflow workflow )
    {
        this.workflow = workflow;
    }

    @Override
    public void body()
    {
        List<Result> failed = workflow.getResults().stream() //
                                      .filter( result -> result.getOutcome() != TaskOutcome.SUCCESS ) //
                                      .collect( Collectors.toList() );

        header( "Test outcome" );
        para( "This page shows results of ", link( "https://fedoraproject.org/wiki/Maven_bootstrapping", "MBI CI" ),
              ", which tests whether Maven can be bootstrapped from scratch." );

        if ( !failed.isEmpty() )
        {
            add( "<p>Test <strong>FAILED</strong></p>" );
            add( "The following tasks failed:" );
            add( "<ul>" );
            for ( Result result : failed )
            {
                add( "<li><strong>", result.getTaskId(), "</strong>" );
                add( "<br/>Reason: ", result.getOutcomeReason(), "<br/>(" );
                for ( Artifact artifact : result.getArtifacts() )
                {
                    if ( artifact.getType() == ArtifactType.LOG || artifact.getType() == ArtifactType.CONFIG )
                    {
                        add( link( result.getTaskId() + "/" + artifact.getName(), artifact.getName() ) );
                    }
                }
                add( ")</li>" );
            }
            add( "</ul>" );
        }
        else if ( workflow.getResults().size() == workflow.getTasks().size() )
        {
            para( "Test <strong>PASSED</strong>" );
        }
        else
        {
            para( "Test is still running. Results will appear here once the test finishes." );
        }

        para( "Tests consists of a set of tasks, which all must be successfully completed in order for the test to pass. ",
              "Constituent tasks are steps necessary to build RPM packages from sources specified by ",
              link( "subject.html", "test subject" ), " on given ",
              link( "platform.html", "operating system platform" ), ", in the way defined by ",
              link( "plan.html", "test plan" ), "." );
        para( "Detailed machine-readable information about test results in XML format can be found in ",
              link( "workflow.xml", "workflow.xml" ), "." );

        footer();

    }
}
