/*-
 * Copyright (c) 2023 Red Hat, Inc.
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

import org.fedoraproject.mbi.wf.model.Task;

/**
 * @author Mikolaj Izdebski
 */
public class BatchLogger
    implements Logger
{
    private void log( Object... args )
    {
        StringBuilder sb = new StringBuilder();

        for ( Object arg : args )
        {
            sb.append( arg.toString() );
        }

        System.err.println( sb );
    }

    @Override
    public void logTaskRunning( Task task )
    {
        log( task, " running" );
    }

    @Override
    public void logTaskSucceeded( FinishedTask finishedTask )
    {
        log( finishedTask.getTask(), " finished; outcome is ", finishedTask.getResult().getOutcome(), ", reason: ",
             finishedTask.getResult().getOutcomeReason() );
    }

    @Override
    public void logTaskFailed( FinishedTask finishedTask )
    {
        log( finishedTask.getTask(), " finished; outcome is ", finishedTask.getResult().getOutcome(), ", reason: ",
             finishedTask.getResult().getOutcomeReason() );
    }

    @Override
    public void logTaskReused( FinishedTask finishedTask )
    {
        log( finishedTask.getTask(), " cached result was reused" );
    }

    @Override
    public void logWorkflowSucceeded()
    {
        log( "Workflow complete" );
    }

    @Override
    public void logWorkflowFailed()
    {
        log( "Workflow INCOMPLETE" );
    }
}
