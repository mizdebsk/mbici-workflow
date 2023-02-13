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
class InteractiveLogger
    implements Logger
{
    private static enum Color
    {
        RUNNING( "34" ), FAILED( "31" ), SUCCEEDED( "32" ), REUSED( "36" ), RESET( "" );

        private Color( String s )
        {
            escapeCode = "\033[" + s + "m";
        }

        private final String escapeCode;

        @Override
        public String toString()
        {
            return escapeCode;
        }
    }

    private final int taskCount;

    private int nRunning;

    private int nSucceeded;

    private int nFailed;

    private int len;

    public InteractiveLogger( int taskCount )
    {
        this.taskCount = taskCount;
    }

    private void log( Color color, Object... args )
    {
        StringBuilder sb = new StringBuilder();

        sb.append( '\r' );
        sb.append( new String( new byte[len] ).replace( '\0', ' ' ) );
        sb.append( '\r' );

        sb.append( color );
        for ( Object arg : args )
        {
            sb.append( arg.toString() );
        }
        sb.append( Color.RESET );
        sb.append( '\n' );

        len = sb.length();
        sb.append( "[ Tasks: " );
        if ( nRunning != 0 )
        {
            sb.append( "running: " ).append( Color.RUNNING ).append( nRunning ).append( Color.RESET ).append( ", " );
        }
        if ( nSucceeded != 0 )
        {
            sb.append( "succeeded: " ).append( Color.SUCCEEDED ).append( nSucceeded ).append( Color.RESET ).append( ", " );
        }
        if ( nFailed != 0 )
        {
            sb.append( "failed: " ).append( Color.FAILED ).append( nFailed ).append( Color.RESET ).append( ", " );
        }
        sb.append( "all: " ).append( taskCount ).append( " ]" );
        len = sb.length() - len;

        System.err.print( sb );
    }

    @Override
    public void logTaskRunning( Task task )
    {
        nRunning++;
        log( Color.RUNNING, task, " running" );
    }

    @Override
    public void logTaskSucceeded( FinishedTask finishedTask )
    {
        nRunning--;
        nSucceeded++;
        log( Color.SUCCEEDED, finishedTask.getTask(), " finished; outcome is ", finishedTask.getResult().getOutcome(),
             ", reason: ", finishedTask.getResult().getOutcomeReason() );
    }

    @Override
    public void logTaskFailed( FinishedTask finishedTask )
    {
        nRunning--;
        nFailed++;
        log( Color.FAILED, finishedTask.getTask(), " finished; outcome is ", finishedTask.getResult().getOutcome(),
             ", reason: ", finishedTask.getResult().getOutcomeReason() );
    }

    @Override
    public void logTaskReused( FinishedTask finishedTask )
    {
        nSucceeded++;
        log( Color.REUSED, finishedTask.getTask(), " cached result was reused" );
    }

    @Override
    public void logWorkflowSucceeded()
    {
        log( Color.SUCCEEDED, "Workflow complete" );
        System.err.println();
    }

    @Override
    public void logWorkflowFailed()
    {
        log( Color.FAILED, "Workflow INCOMPLETE" );
        System.err.println();
    }
}
