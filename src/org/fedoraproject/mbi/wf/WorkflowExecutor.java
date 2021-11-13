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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.fedoraproject.mbi.wf.model.Task;
import org.fedoraproject.mbi.wf.model.TaskOutcome;
import org.fedoraproject.mbi.wf.model.Workflow;
import org.fedoraproject.mbi.wf.model.WorkflowBuilder;

/**
 * @author Mikolaj Izdebski
 */
public class WorkflowExecutor
{
    private final CacheManager cacheManager;

    private final WorkflowBuilder workflowBuilder = new WorkflowBuilder();

    private final Set<Task> newTasks;

    private final Set<Task> pendingOrRunningTasks = new LinkedHashSet<>();

    private final Set<FinishedTask> successfullyFinishedTasks = new LinkedHashSet<>();

    private final Set<FinishedTask> unsuccessfullyFinishedTasks = new LinkedHashSet<>();

    private final Logger logger = new Logger();

    private final Throttle throttle = new Throttle();

    private final Dumper dumper;

    public WorkflowExecutor( Workflow wf, Path wfPath, CacheManager cacheManager )
    {
        wf.getTasks().stream().forEach( workflowBuilder::addTask );
        newTasks = new LinkedHashSet<>( wf.getTasks() );
        dumper = new Dumper( wfPath );
        this.cacheManager = cacheManager;
    }

    public CacheManager getCacheManager()
    {
        return cacheManager;
    }

    public Throttle getThrottle()
    {
        return throttle;
    }

    public synchronized void stateChangeFromPendingToRunning( Task task )
    {
        logger.logTaskRunning( task );
    }

    public synchronized void stateChangeFromRunningToFinished( FinishedTask finishedTask )
    {
        workflowBuilder.addResult( finishedTask.getResult() );
        pendingOrRunningTasks.remove( finishedTask.getTask() );
        if ( finishedTask.getResult().getOutcome() == TaskOutcome.SUCCESS )
        {
            successfullyFinishedTasks.add( finishedTask );
            logger.logTaskSucceeded( finishedTask );
        }
        else
        {
            unsuccessfullyFinishedTasks.add( finishedTask );
            logger.logTaskFailed( finishedTask );
        }
        dumper.dumpEventually( workflowBuilder.build() );
        notify();
    }

    public synchronized void stateChangeFromPendingToFinished( FinishedTask finishedTask )
    {
        workflowBuilder.addResult( finishedTask.getResult() );
        pendingOrRunningTasks.remove( finishedTask.getTask() );
        successfullyFinishedTasks.add( finishedTask );
        dumper.dumpEventually( workflowBuilder.build() );
        notify();
        logger.logTaskReused( finishedTask );
    }

    public synchronized Workflow execute()
        throws ReflectiveOperationException
    {
        List<Thread> threads = new ArrayList<>();
        dumper.start();
        threads.add( dumper );
        outer: for ( ;; )
        {
            for ( Task td : newTasks )
            {
                List<FinishedTask> deps = new ArrayList<>();
                for ( String depId : td.getDependencies() )
                {
                    for ( FinishedTask task : successfullyFinishedTasks )
                    {
                        if ( task.getTask().getId().equals( depId ) )
                        {
                            deps.add( task );
                        }
                    }
                }

                if ( deps.size() == td.getDependencies().size() )
                {
                    newTasks.remove( td );
                    pendingOrRunningTasks.add( td );

                    Thread thread = new TaskExecution( this, td, deps );
                    thread.start();
                    threads.add( thread );
                    continue outer;
                }
            }
            if ( !pendingOrRunningTasks.isEmpty() )
            {
                try
                {
                    wait();
                }
                catch ( InterruptedException e )
                {
                    for ( Thread thread : threads )
                    {
                        thread.interrupt();
                    }
                }
                continue;
            }
            break;
        }
        if ( newTasks.isEmpty() && pendingOrRunningTasks.isEmpty() )
        {
            logger.logWorkflowSucceeded();
        }
        else
        {
            logger.logWorkflowFailed();
        }
        dumper.terminate();
        for ( Thread thread : threads )
        {
            try
            {
                thread.join();
            }
            catch ( InterruptedException e )
            {
                for ( Thread thread2 : threads )
                {
                    thread2.interrupt();
                }
            }
        }
        for ( Thread thread : threads )
        {
            try
            {
                thread.join();
            }
            catch ( InterruptedException e )
            {
            }
        }

        return workflowBuilder.build();
    }
}
