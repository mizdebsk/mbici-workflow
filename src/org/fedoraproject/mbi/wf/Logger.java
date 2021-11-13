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

import org.fedoraproject.mbi.wf.model.Task;

/**
 * @author Mikolaj Izdebski
 */
class Logger
{
    public void logTaskRunning( Task task )
    {
        System.err.println( "\033[34m" + task + " running" + "\033[m" );
    }

    public void logTaskSucceeded( FinishedTask finishedTask )
    {
        System.err.println( "\033[32m" + finishedTask.getTask() + " finished; outcome is "
            + finishedTask.getResult().getOutcome() + ", reason: " + finishedTask.getResult().getOutcomeReason()
            + "\033[m" );
    }

    public void logTaskFailed( FinishedTask finishedTask )
    {
        System.err.println( "\033[31m" + finishedTask.getTask() + " finished; outcome is "
            + finishedTask.getResult().getOutcome() + ", reason: " + finishedTask.getResult().getOutcomeReason()
            + "\033[m" );
    }

    public void logTaskReused( FinishedTask finishedTask )
    {
        System.err.println( "\033[36m" + finishedTask.getTask() + " cached result was reused" + "\033[m" );
    }

    public void logWorkflowSucceeded()
    {
        System.err.println( "Workflow complete" );
    }

    public void logWorkflowFailed()
    {
        System.err.println( "Workflow INCOMPLETE" );
    }
}
