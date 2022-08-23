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
package org.fedoraproject.mbi.wf.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.fedoraproject.mbi.wf.ArtifactManager;
import org.fedoraproject.mbi.wf.TaskExecution;
import org.fedoraproject.mbi.wf.TaskHandler;
import org.fedoraproject.mbi.wf.TaskTermination;
import org.fedoraproject.mbi.wf.model.ArtifactType;

/**
 * @author Mikolaj Izdebski
 */
public class ValidateTaskHandler
    implements TaskHandler
{
    private static final String PLAN_NAME = "/plans/javapackages";

    @Override
    public void handleTask( TaskExecution task )
        throws TaskTermination
    {
        ArtifactManager am = task.getArtifactManager();
        Path sourceDir = am.getDepArtifactsByType( ArtifactType.CHECKOUT, task ).iterator().next();
        Path srpm = am.getDepArtifactsByType( ArtifactType.SRPM, task ).iterator().next();
        List<Path> rpms = am.getDepArtifactsByType( ArtifactType.RPM, task );

        Path tmtWorkDir = task.getWorkDir().resolve( "tmt" );
        Path testArtifactsDir = task.getWorkDir().resolve( "test-artifacts" );

        try
        {
            Files.createDirectory( testArtifactsDir );
            Files.copy( srpm, testArtifactsDir.resolve( srpm.getFileName() ) );
            for ( Path rpm : rpms )
            {
                Files.copy( rpm, testArtifactsDir.resolve( rpm.getFileName() ) );
            }

            Command tmt = new Command( "tmt" );
            tmt.addArg( "--root", sourceDir.toString() );
            tmt.addArg( "-c", "trigger=MBICI" );
            tmt.addArg( "run" );
            // tmt.addArg( "--scratch" );
            tmt.addArg( "--id", tmtWorkDir.toString() );
            tmt.addArg( "-e", "TEST_ARTIFACTS=" + testArtifactsDir );
            tmt.addArg( "-e", "JP_VALIDATOR_IMAGE=javapackages-validator" );
            tmt.addArg( "-e", "ENVROOT=/srv" );
            tmt.addArg( "discover" );
            // TODO checkout javapackages-tests and then use local repo
            // so that each test run doesn't to have clone the git repo
            // tmt.addArg( "--how", "fmf" );
            // tmt.addArg( "--url", "/path/to/local/javapackages-tests" );
            // tmt.addArg( "--ref", "master" );
            tmt.addArg( "plan" );
            tmt.addArg( "--name", PLAN_NAME );
            tmt.addArg( "provision" );
            tmt.addArg( "--how", "local" );
            tmt.addArg( "execute" );
            tmt.addArg( "--how", "tmt" );
            tmt.addArg( "--no-progress-bar" );
            // TODO add html or junit report
            tmt.addArg( "report" );
            tmt.addArg( "-vvv" );
            tmt.run( task, 120 );

            TaskTermination.success( "Validation was successful" );
        }
        catch ( IOException e )
        {
            TaskTermination.error( "I/O error during validation: " + e.getMessage() );
        }
    }
}
