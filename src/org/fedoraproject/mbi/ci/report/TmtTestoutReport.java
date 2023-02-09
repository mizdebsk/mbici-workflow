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
package org.fedoraproject.mbi.ci.report;

import org.fedoraproject.mbi.wf.model.Result;

/**
 * Produces testout.log file with compact description of test result.
 * 
 * @author Mikolaj Izdebski
 */
public class TmtTestoutReport
    extends Report
{
    private final Result result;

    public TmtTestoutReport( Result result )
    {
        this.result = result;
    }

    @Override
    public void body()
    {
        add( "Task: " + result.getTaskId() );
        add( "Time started: " + result.getTimeStarted() );
        add( "Time finished: " + result.getTimeFinished() );
        add( "Outcome: " + result.getOutcome() );
        add( "Outcome reason: " + result.getOutcomeReason() );
        add( "More details are available in log files." );
    }
}
