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

import io.kojan.mbici.cache.ArtifactType;
import io.kojan.workflow.model.Artifact;
import io.kojan.workflow.model.Result;
import io.kojan.workflow.model.TaskOutcome;
import io.kojan.workflow.model.Workflow;
import java.time.Duration;

/// Produces results.yaml file in the [format expected by
/// tmt](https://tmt.readthedocs.io/en/stable/spec/tests.html#result).
///
/// @author Mikolaj Izdebski
public class TmtResultsReport extends Report {
    private final Workflow workflow;

    public TmtResultsReport(Workflow workflow) {
        this.workflow = workflow;
    }

    @Override
    public void body() {
        boolean failed =
                workflow.getResults().stream()
                        .filter(result -> result.getOutcome() != TaskOutcome.SUCCESS)
                        .findAny()
                        .isPresent();

        add("- name: /overview");
        add("  result: " + (failed ? "fail" : "pass"));
        add("  log:");
        add("    - ../output.txt");
        add("    - result.html");

        for (Result result : workflow.getResults()) {
            String tmtOutcome =
                    switch (result.getOutcome()) {
                        case SUCCESS -> "pass";
                        case FAILURE -> "fail";
                        case ERROR -> "error";
                    };

            Duration duration = Duration.between(result.getTimeStarted(), result.getTimeFinished());
            String durationString =
                    "%d:%02d:%02d"
                            .formatted(
                                    duration.toHours(),
                                    duration.toMinutesPart(),
                                    duration.toSecondsPart());

            add("- name: /task/" + result.getTaskId());
            add("  result: " + tmtOutcome);
            add("  duration: " + durationString);
            add("  log:");

            add("    - " + result.getTaskId() + "/testout.log");

            for (Artifact artifact : result.getArtifacts()) {
                if (artifact.getType().equals(ArtifactType.LOG)
                        || artifact.getType().equals(ArtifactType.CONFIG)) {
                    add("    - " + result.getTaskId() + "/" + artifact.getName());
                }
            }
        }
    }
}
