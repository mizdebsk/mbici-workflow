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

import org.fedoraproject.mbi.wf.ArtifactManager;
import org.fedoraproject.mbi.wf.TaskExecution;
import org.fedoraproject.mbi.wf.TaskHandler;
import org.fedoraproject.mbi.wf.TaskTermination;
import org.fedoraproject.mbi.wf.model.ArtifactType;
import org.fedoraproject.mbi.wf.model.Parameter;

/**
 * @author Mikolaj Izdebski
 */
public class RpmTaskHandler
    implements TaskHandler
{
    @Override
    public void handleTask( TaskExecution task )
        throws TaskTermination
    {
        ArtifactManager am = task.getArtifactManager();
        Path srpmPath = am.getDepArtifactsByType( ArtifactType.SRPM, task ).iterator().next();
        Mock mock = new Mock();
        for ( Parameter param : task.getTask().getParameters() )
        {
            mock.addMacro( param.getName(), param.getValue() );
        }
        mock.run( task, "--rebuild", srpmPath.toString() );
        try ( var s = Files.find( task.getWorkDir(), 1, ( p, bfa ) -> p.getFileName().toString().endsWith( ".rpm" )
            && !p.getFileName().toString().endsWith( ".src.rpm" ) && bfa.isRegularFile() ) )
        {
            for ( var it = s.iterator(); it.hasNext(); )
            {
                am.copyArtifact( ArtifactType.RPM, it.next() );
            }
        }
        catch ( IOException e )
        {
            throw TaskTermination.error( "I/O error when looknig for RPM files: " + e.getMessage() );
        }
        TaskTermination.success( "Binary RPMs were built in mock" );
    }
}
