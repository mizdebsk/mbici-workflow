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
package org.fedoraproject.mbi.wf.handler;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.fedoraproject.mbi.wf.ArtifactManager;
import org.fedoraproject.mbi.wf.Kubernetes;
import org.fedoraproject.mbi.wf.TaskExecution;
import org.fedoraproject.mbi.wf.TaskTermination;
import org.fedoraproject.mbi.wf.model.ArtifactType;

/**
 * @author Mikolaj Izdebski
 */
class Command
{
    private Path workDir;

    private final List<String> cmd = new ArrayList<>();

    public Command( String... args )
    {
        addArg( args );
    }

    public Command( List<String> args )
    {
        addArg( args );
    }

    public void addArg( String... args )
    {
        for ( String arg : args )
        {
            cmd.add( arg );
        }
    }

    public void addArg( List<String> args )
    {
        cmd.addAll( args );
    }

    public List<String> getArgs()
    {
        return Collections.unmodifiableList( cmd );
    }

    public void setWorkDir( Path workDir )
    {
        this.workDir = workDir;
    }

    public void run( TaskExecution taskExecution, int timeoutSeconds )
        throws TaskTermination
    {
        ProcessBuilder pb = new ProcessBuilder( cmd );
        pb.directory( ( workDir != null ? workDir : taskExecution.getWorkDir() ).toFile() );
        pb.redirectInput( Paths.get( "/dev/null" ).toFile() );
        pb.redirectOutput( getLog( taskExecution, "stdout.log" ) );
        pb.redirectError( getLog( taskExecution, "stderr.log" ) );
        Process process;
        try
        {
            process = pb.start();
        }
        catch ( IOException e )
        {
            TaskTermination.error( "I/O error while trying to run command: " + e.getMessage() );
            return;
        }
        try
        {
            if ( !process.waitFor( timeoutSeconds, TimeUnit.SECONDS ) )
            {
                TaskTermination.error( "Timeout waiting for " + cmd.get( 0 ) );
                return;
            }
        }
        catch ( InterruptedException e )
        {
            TaskTermination.error( "Interrupted while waiting for command to finish" );
        }
        finally
        {
            process.destroy();
        }
        if ( process.exitValue() != 0 )
        {
            TaskTermination.fail( cmd.get( 0 ) + " exited with code " + process.exitValue() );
            return;
        }
    }

    public void runRemote( TaskExecution taskExecution, int timeoutSeconds )
        throws TaskTermination
    {
        Optional<Kubernetes> kubernetes = taskExecution.getKubernetes();
        if ( kubernetes.isPresent() )
        {
            Command kubectl = new Command( kubernetes.get().wrapCommand( taskExecution, cmd ) );
            kubectl.run( taskExecution, 3600 );
        }
        else
        {
            run( taskExecution, timeoutSeconds );
        }
    }

    public Redirect getLog( TaskExecution taskExecution, String fileName )
        throws TaskTermination
    {
        ArtifactManager artifactManager = taskExecution.getArtifactManager();
        Path logPath = artifactManager.getOrCreate( ArtifactType.LOG, fileName );
        if ( Files.exists( logPath ) )
        {
            return Redirect.appendTo( logPath.toFile() );
        }
        else
        {
            return Redirect.to( logPath.toFile() );
        }
    }
}
