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
package org.fedoraproject.mbi.wf.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.fedoraproject.mbi.xml.Builder;

/**
 * @author Mikolaj Izdebski
 */
public class ResultBuilder implements Builder<Result> {
    private String id;
    private String taskId;
    private final List<Artifact> artifacts = new ArrayList<>();
    private TaskOutcome outcome;
    private String outcomeReason;
    private LocalDateTime timeStarted;
    private LocalDateTime timeFinished;

    public void setId(String id) {
        this.id = id;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void addArtifact(Artifact artifact) {
        artifacts.add(artifact);
    }

    public void setOutcome(TaskOutcome outcome) {
        this.outcome = outcome;
    }

    public void setOutcomeReason(String outcomeReason) {
        this.outcomeReason = outcomeReason;
    }

    public void setTimeStarted(LocalDateTime timeStarted) {
        this.timeStarted = timeStarted;
    }

    public void setTimeFinished(LocalDateTime timeFinished) {
        this.timeFinished = timeFinished;
    }

    @Override
    public Result build() {
        return new Result(id, taskId, artifacts, outcome, outcomeReason, timeStarted, timeFinished);
    }
}
