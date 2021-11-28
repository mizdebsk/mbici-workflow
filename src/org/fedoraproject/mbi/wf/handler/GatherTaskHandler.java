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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
public class GatherTaskHandler
    implements TaskHandler
{
    private void parseTaskParameters( Task task, List<String> packageNames, Map<String, String> repos )
        throws TaskTermination
    {
        for ( Parameter param : task.getParameters() )
        {
            if ( param.getName().startsWith( "package-" ) )
            {
                packageNames.add( param.getValue() );
            }
            else if ( param.getName().startsWith( "repo-" ) )
            {
                repos.put( param.getName().substring( 5 ), param.getValue() );
            }
            else
            {
                throw TaskTermination.error( "Unknown gather task parameter: " + param.getName() );
            }
        }
    }

    private void downloadPackages( TaskExecution taskExecution, List<String> packageNames, Map<String, String> repos )
        throws TaskTermination
    {
        Command dnf = new Command( "fakeroot", "dnf" );

        dnf.addArg( "--installroot", taskExecution.getWorkDir().toString() );

        ArtifactManager am = taskExecution.getArtifactManager();
        Path dnfConfPath = am.getOrCreate( ArtifactType.CONFIG, "dnf.conf" );
        try ( BufferedWriter bw = Files.newBufferedWriter( dnfConfPath ) )
        {
            bw.write( "[main]\n" );
            bw.write( "assumeyes=1\n" );
            bw.write( "reposdir=/\n" );
            bw.write( "gpgcheck=0\n" );
        }
        catch ( IOException e )
        {
            TaskTermination.error( "I/O error when writing DNF config: " + e.getMessage() );
        }
        dnf.addArg( "-c", dnfConfPath.toString() );

        for ( var repo : repos.entrySet() )
        {
            dnf.addArg( "--repofrompath", repo.getKey() + "," + repo.getValue() );
        }

        dnf.addArg( "install", "--downloadonly" );
        for ( String packageName : packageNames )
        {
            dnf.addArg( packageName );
        }

        dnf.run( taskExecution, 300 );
    }

    @Override
    public void handleTask( TaskExecution taskExecution )
        throws TaskTermination
    {
        ArtifactManager am = taskExecution.getArtifactManager();
        List<String> packageNames = new ArrayList<>();
        Map<String, String> repos = new LinkedHashMap<>();
        parseTaskParameters( taskExecution.getTask(), packageNames, repos );

        downloadPackages( taskExecution, packageNames, repos );

        try ( Stream<Path> pathStream =
            Files.find( taskExecution.getWorkDir().resolve( "var/cache/dnf" ), 10,
                        ( p, bfa ) -> p.getFileName().toString().endsWith( ".rpm" ) && bfa.isRegularFile() ) )
        {
            for ( Iterator<Path> pathIterator = pathStream.iterator(); pathIterator.hasNext(); )
            {
                am.copyArtifact( ArtifactType.RPM, pathIterator.next() );
            }
        }
        catch ( IOException e )
        {
            TaskTermination.error( "I/O error when copying gathered RPM packages to result dir: " + e.getMessage() );
        }

        Path repodatataPath = am.create( ArtifactType.REPO, "repodata" );
        Command createrepo = new Command( "createrepo_c", repodatataPath.getParent().toString() );
        createrepo.run( taskExecution, 30 );

        TaskTermination.success( "Platform repo was downloaded successfully" );
    }
}
