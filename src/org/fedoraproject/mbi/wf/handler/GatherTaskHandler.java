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
package org.fedoraproject.mbi.wf.handler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private void writeDnfConfig( Path dnfConfPath, Map<String, String> repos )
        throws TaskTermination
    {
        try ( BufferedWriter bw = Files.newBufferedWriter( dnfConfPath ) )
        {
            bw.write( "[main]\n" );
            bw.write( "gpgcheck=0\n" );
            bw.write( "reposdir=/\n" );
            bw.write( "install_weak_deps=0\n" );

            for ( var repo : repos.entrySet() )
            {
                bw.write( "\n" );
                bw.write( "[" + repo.getKey() + "]\n" );
                bw.write( "name=" + repo.getKey() + "\n" );
                bw.write( "baseurl=" + repo.getValue() + "\n" );
            }
        }
        catch ( IOException e )
        {
            TaskTermination.error( "I/O error when writing DNF config: " + e.getMessage() );
        }
    }

    private void downloadPackages( TaskExecution taskExecution, List<String> packageNames, Path downloadDir,
                                   Path dnfConfPath )
        throws TaskTermination
    {
        Command dnf = new Command( "dnf" );
        dnf.addArg( "--releasever", "dummy" );
        dnf.addArg( "--installroot", taskExecution.getWorkDir().toString() );
        dnf.addArg( "--config", dnfConfPath.toString() );
        dnf.addArg( "download", "--resolve", "--alldeps" );
        dnf.addArg( packageNames );
        dnf.setWorkDir( downloadDir );
        dnf.runRemote( taskExecution, 600 );
    }

    @Override
    public void handleTask( TaskExecution taskExecution )
        throws TaskTermination
    {
        List<String> packageNames = new ArrayList<>();
        Map<String, String> repos = new LinkedHashMap<>();
        parseTaskParameters( taskExecution.getTask(), packageNames, repos );

        Path dnfConfPath = taskExecution.addArtifact( ArtifactType.CONFIG, "dnf.conf" );
        writeDnfConfig( dnfConfPath, repos );

        downloadPackages( taskExecution, packageNames, taskExecution.getResultDir(), dnfConfPath );

        try ( var s =
            Files.find( taskExecution.getResultDir(), 1, ( p, bfa ) -> p.getFileName().toString().endsWith( ".rpm" )
                && !p.getFileName().toString().endsWith( ".src.rpm" ) && bfa.isRegularFile() ) )
        {
            for ( var it = s.iterator(); it.hasNext(); )
            {
                taskExecution.addArtifact( ArtifactType.RPM, it.next().getFileName().toString() );
            }
        }
        catch ( IOException e )
        {
            throw TaskTermination.error( "I/O error when looknig for RPM files: " + e.getMessage() );
        }

        TaskTermination.success( "Platform repo was downloaded successfully" );
    }
}
