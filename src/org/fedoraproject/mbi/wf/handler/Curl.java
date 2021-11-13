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

import java.nio.file.Path;

import org.fedoraproject.mbi.wf.TaskExecution;
import org.fedoraproject.mbi.wf.TaskTermination;

/**
 * @author Mikolaj Izdebski
 */
class Curl
{
    private final TaskExecution task;

    public Curl( TaskExecution task )
    {
        this.task = task;
    }

    public void downloadFile( String url, Path targetPath )
        throws TaskTermination
    {
        Command command = new Command( task, 120, "curl", "-f", "-L", "-o", targetPath.toString(), url );
        command.run();
    }
}
