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

import java.nio.file.Path;

import org.fedoraproject.mbi.wf.model.Artifact;
import org.fedoraproject.mbi.wf.model.Result;
import org.fedoraproject.mbi.wf.model.Task;

/**
 * @author Mikolaj Izdebski
 */
public class FinishedTask
{
    private final Task task;

    private final Result result;

    private final Path resultDir;

    public FinishedTask( Task task, Result result, Path resultDir )
    {
        this.task = task;
        this.result = result;
        this.resultDir = resultDir;
    }

    public Task getTask()
    {
        return task;
    }

    public Result getResult()
    {
        return result;
    }

    public Path getArtifact( Artifact artifact )
    {
        return resultDir.resolve( artifact.getName() );
    }
}
