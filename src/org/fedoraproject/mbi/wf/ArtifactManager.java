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
package org.fedoraproject.mbi.wf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fedoraproject.mbi.wf.model.Artifact;
import org.fedoraproject.mbi.wf.model.ArtifactType;

/**
 * @author Mikolaj Izdebski
 */
public class ArtifactManager
{
    private final Path resultDir;

    private final List<Artifact> artifacts = new ArrayList<>();

    public ArtifactManager( Path resultDir )
    {
        this.resultDir = resultDir;
    }

    public List<Artifact> getArtifacts()
    {
        return Collections.unmodifiableList( artifacts );
    }

    public Path getByName( String name )
    {
        for ( Artifact descriptor : artifacts )
        {
            if ( descriptor.getName().equals( name ) )
            {
                return resultDir.resolve( descriptor.getName() );
            }
        }

        return null;
    }

    public List<Path> getDepArtifactsByType( ArtifactType type, TaskExecution task )
    {
        List<Path> artifacts = new ArrayList<>();

        for ( FinishedTask dependency : task.getDependencies() )
        {
            for ( Artifact dependencyArtifact : dependency.getResult().getArtifacts() )
            {
                if ( dependencyArtifact.getType().equals( type ) )
                {
                    artifacts.add( dependency.getResultDir().resolve( dependencyArtifact.getName() ) );
                }
            }
        }

        return artifacts;
    }

    public Path create( ArtifactType type, String name )
    {
        Artifact artifact = new Artifact( type, name );
        artifacts.add( artifact );
        return resultDir.resolve( artifact.getName() );
    }

    public Path getOrCreate( ArtifactType type, String name )
    {
        Path artifact = getByName( name );

        if ( artifact == null )
        {
            artifact = create( type, name );
        }

        return artifact;
    }

    public void copyArtifact( ArtifactType type, Path sourcePath )
        throws TaskTermination
    {
        String name = sourcePath.getFileName().toString();
        Path artifact = create( type, name );

        try
        {
            Files.copy( sourcePath, artifact );
        }
        catch ( IOException e )
        {
            TaskTermination.error( "I/O error when copying artifact " + artifact + ": " + e.getMessage() );
        }
    }

    public void moveArtifact( ArtifactType type, Path sourcePath )
        throws TaskTermination
    {
        String name = sourcePath.getFileName().toString();
        Path artifact = create( type, name );

        try
        {
            Files.move( sourcePath, artifact );
        }
        catch ( IOException e )
        {
            TaskTermination.error( "I/O error when moving artifact " + artifact + ": " + e.getMessage() );
        }
    }

    public void symlinkArtifact( ArtifactType type, Path sourcePath )
        throws TaskTermination
    {
        String name = sourcePath.getFileName().toString();
        Path artifact = create( type, name );

        try
        {
            Files.createSymbolicLink( artifact, sourcePath );
        }
        catch ( IOException e )
        {
            TaskTermination.error( "I/O error when linking artifact " + artifact + ": " + e.getMessage() );
        }
    }
}
