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
package io.kojan.mbici.generate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.kojan.mbici.model.Phase;
import io.kojan.mbici.model.Plan;
import io.kojan.mbici.model.Platform;
import io.kojan.mbici.model.Subject;
import io.kojan.mbici.model.SubjectComponent;
import io.kojan.workflow.model.Task;
import io.kojan.workflow.model.Workflow;
import io.kojan.workflow.model.WorkflowBuilder;

/**
 * @author Mikolaj Izdebski
 */
class WorkflowFactory {
    public Workflow createFromPlan(Platform platform, Plan plan, Subject subject) {
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        TaskFactory taskFactory = new TaskFactory(workflowBuilder);
        Map<String, Task> srpms = new LinkedHashMap<>();
        Map<String, Task> checkouts = new LinkedHashMap<>();

        Task gather = taskFactory.createGatherTask(platform);
        Task gatherRepo = taskFactory.createRepoTask("platform", Collections.singletonList(gather));

        LinkedList<Task> repos = new LinkedList<>();
        repos.add(gatherRepo);

        for (Phase phase : plan.getPhases()) {
            List<Task> rpms = new ArrayList<>();

            for (String component : phase.getComponents()) {
                Task srpm = srpms.get(component);
                if (srpm == null) {
                    SubjectComponent componentSubject = subject.getSubjectComponent(component);
                    Task checkout = taskFactory.createCheckoutTask(componentSubject);
                    checkouts.put(component, checkout);
                    srpm = taskFactory.createSrpmTask(component, checkout, gatherRepo);
                    srpms.put(component, srpm);
                }

                Task rpm = taskFactory.createRpmTask(component, phase.getName(), srpm, repos, plan.getMacros(),
                        phase.getMacros());
                rpms.add(rpm);
            }

            Task repo = taskFactory.createRepoTask(phase.getName(), rpms);
            repos.addFirst(repo);
        }

        return workflowBuilder.build();
    }
}
