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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.fedoraproject.mbi.wf.ArtifactManager;
import org.fedoraproject.mbi.wf.TaskExecution;
import org.fedoraproject.mbi.wf.TaskHandler;
import org.fedoraproject.mbi.wf.TaskTermination;
import org.fedoraproject.mbi.wf.model.ArtifactType;

/**
 * @author Mikolaj Izdebski
 */
public class RepoTaskHandler
    implements TaskHandler
{
    @Override
    public void handleTask( TaskExecution taskExecution )
        throws TaskTermination
    {
        ArtifactManager am = taskExecution.getArtifactManager();
        Path repoPath = am.create( ArtifactType.REPO, "repo" );
        try
        {
            Files.createDirectories( repoPath );
        }
        catch ( IOException e )
        {
            TaskTermination.error( "I/O error when creating directory " + repoPath + ": " + e.getMessage() );
        }

        for ( Path rpmPath : am.getDepArtifactsByType( ArtifactType.RPM, taskExecution ) )
        {
            Path rpmLinkPath = repoPath.resolve( rpmPath.getFileName() );

            try
            {
                Files.createSymbolicLink( rpmLinkPath, rpmPath );
            }
            catch ( IOException e )
            {
                TaskTermination.error( "I/O error when creating symbolic link " + rpmLinkPath + ": " + e.getMessage() );
            }
        }

        Createrepo createrepo = new Createrepo( taskExecution );
        createrepo.run( repoPath );

        TaskTermination.success( "Repo created successfully" );
    }
}
