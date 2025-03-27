/*-
 * Copyright (c) 2023-2025 Red Hat, Inc.
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
package io.kojan.mbici.report;

import io.kojan.workflow.FinishedTask;
import io.kojan.workflow.model.Parameter;
import io.kojan.workflow.model.Result;
import io.kojan.workflow.model.Task;

/// Produces `testout.log` file with compact description of test result.
///
/// @author Mikolaj Izdebski
public class TmtTestoutReport extends Report {
    private final FinishedTask finishedTask;

    public TmtTestoutReport(FinishedTask finishedTask) {
        this.finishedTask = finishedTask;
    }

    @Override
    public void body() {
        Task task = finishedTask.getTask();
        Result result = finishedTask.getResult();

        add("Task ID: " + result.getTaskId());
        add("Result ID: " + result.getId());
        add("Time started: " + result.getTimeStarted());
        add("Time finished: " + result.getTimeFinished());
        add("Outcome: " + result.getOutcome());
        add("Outcome reason: " + result.getOutcomeReason());
        add("More details are available in log files.");

        add("");
        add("Task handler: " + task.getHandler());

        add("");
        add("Task parameters:");
        for (Parameter param : task.getParameters()) {
            add("  " + param.getName() + ": " + param.getValue());
        }

        add("");
        add("Task dependencies:");
        for (String dep : task.getDependencies()) {
            add("  " + dep);
        }
    }
}
