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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fedoraproject.mbi.wf.ArtifactManager;
import org.fedoraproject.mbi.wf.TaskExecution;
import org.fedoraproject.mbi.wf.TaskHandler;
import org.fedoraproject.mbi.wf.TaskTermination;
import org.fedoraproject.mbi.wf.model.ArtifactType;
import org.fedoraproject.mbi.wf.model.Parameter;
import org.fedoraproject.mbi.wf.model.Task;

/**
 * @author Mikolaj Izdebski
 */
public class CheckoutTaskHandler
    implements TaskHandler
{
    private String scm;

    private String commit;

    private String lookaside;

    private void parseTaskParameters( Task task )
        throws TaskTermination
    {
        for ( Parameter param : task.getParameters() )
        {
            switch ( param.getName() )
            {
                case "scm":
                    scm = param.getValue();
                    break;
                case "commit":
                    commit = param.getValue();
                    break;
                case "lookaside":
                    lookaside = param.getValue();
                    break;
                default:
                    throw TaskTermination.fail( "Unknown gather task parameter: " + param.getName() );
            }
        }

        if ( scm == null )
        {
            TaskTermination.fail( "Mandatory parameter scm was not provided" );
        }
        if ( commit == null )
        {
            TaskTermination.fail( "Mandatory parameter commit was not provided" );
        }
        if ( lookaside == null )
        {
            TaskTermination.fail( "Mandatory parameter lookaside was not provided" );
        }
    }

    private void runGit( TaskExecution task, String... args )
        throws TaskTermination
    {
        Command cmd = new Command( task, 60, "git" );
        cmd.addArg( "--git-dir", task.getWorkDir().resolve( "git" ).toString() );
        cmd.addArg( args );
        cmd.run();
    }

    public void handleTask0( TaskExecution task )
        throws TaskTermination, IOException
    {
        ArtifactManager am = task.getArtifactManager();
        Path dgCache = task.getCacheManager().getDistGit( commit );
        am.symlinkArtifact( ArtifactType.CHECKOUT, dgCache );
        if ( Files.exists( dgCache ) )
        {
            TaskTermination.success( "Commit was found in dist-git cache" );
            return;
        }
        Path workTree = task.getCacheManager().createPending( "checkout-" + commit );
        runGit( task, "init", "--bare" );
        runGit( task, "remote", "add", "-f", "origin", scm );
        Files.createDirectories( workTree );
        runGit( task, "--work-tree", workTree.toString(), "reset", "--hard", commit );
        for ( String line : Files.readAllLines( workTree.resolve( "sources" ) ) )
        {
            Pattern pattern = Pattern.compile( "^SHA512 \\(([^)]+)\\) = ([0-9a-f]{128})$" );
            Matcher matcher = pattern.matcher( line );
            if ( matcher.matches() )
            {
                String fileName = matcher.group( 1 );
                String hash = matcher.group( 2 );
                Path lasCache = task.getCacheManager().getLookaside( hash );
                Path downloadPath = workTree.resolve( fileName );
                if ( !Files.exists( lasCache ) )
                {
                    //System.err.println( "Downloading " + fileName );
                    String url = lookaside + "/" + fileName + "/sha512/" + hash + "/" + fileName;
                    Curl curl = new Curl( task );
                    curl.downloadFile( url, downloadPath );
                    Files.move( downloadPath, lasCache );
                }
                Files.createLink( downloadPath, lasCache );
            }
        }
        Files.move( workTree, dgCache );
        TaskTermination.success( "Commit was checked out from SCM" );
    }

    @Override
    public void handleTask( TaskExecution task )
        throws TaskTermination
    {
        parseTaskParameters( task.getTask() );
        try
        {
            handleTask0( task );
        }
        catch ( IOException e )
        {
            TaskTermination.error( "I/O error during checkout: " + e.getMessage() );
        }
    }
}
