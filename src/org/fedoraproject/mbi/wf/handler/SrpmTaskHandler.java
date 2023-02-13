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
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;

import org.fedoraproject.mbi.wf.TaskExecution;
import org.fedoraproject.mbi.wf.TaskHandler;
import org.fedoraproject.mbi.wf.TaskTermination;
import org.fedoraproject.mbi.wf.model.ArtifactType;
import org.fedoraproject.mbi.wf.model.Task;

/**
 * @author Mikolaj Izdebski
 */
public class SrpmTaskHandler
    implements TaskHandler
{
    public SrpmTaskHandler( Task task )
    {
        if ( !task.getParameters().isEmpty() )
        {
            throw new IllegalArgumentException( getClass().getName() + " does not take any parameters" );
        }
    }

    private Path findOneFile( Path baseDir, String extension )
        throws TaskTermination
    {
        try ( var s = Files.find( baseDir, 1,
                                  ( p, bfa ) -> p.getFileName().toString().endsWith( extension ) && bfa.isRegularFile(),
                                  FileVisitOption.FOLLOW_LINKS ) )
        {
            var it = s.iterator();
            if ( !it.hasNext() )
            {
                throw TaskTermination.fail( "No " + extension + " file was found in " + baseDir );
            }

            Path path = it.next();

            if ( it.hasNext() )
            {
                throw TaskTermination.fail( "More than one " + extension + " file was not found in " + baseDir );
            }

            return path;
        }
        catch ( IOException e )
        {
            throw TaskTermination.error( "I/O error when looknig for " + extension + " file: " + e.getMessage() );
        }
    }

    @Override
    public void handleTask( TaskExecution taskExecution )
        throws TaskTermination
    {
        Path sourcePath = taskExecution.getDependencyArtifact( ArtifactType.CHECKOUT );
        Path specPath = findOneFile( sourcePath, ".spec" );
        Mock mock = new Mock();
        mock.run( taskExecution, "--buildsrpm", "--spec", specPath.toString(), "--sources", sourcePath.toString() );
        Path srpmPath = findOneFile( taskExecution.getResultDir(), ".src.rpm" );
        taskExecution.addArtifact( ArtifactType.SRPM, srpmPath.getFileName().toString() );
        TaskTermination.success( "Source RPM was built in mock" );
    }
}
