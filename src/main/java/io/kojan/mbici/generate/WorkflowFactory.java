/*-
 * Copyright (c) 2021-2025 Red Hat, Inc.
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

import io.kojan.mbici.model.Phase;
import io.kojan.mbici.model.Plan;
import io.kojan.mbici.model.Platform;
import io.kojan.mbici.model.Subject;
import io.kojan.mbici.model.SubjectComponent;
import io.kojan.workflow.model.Task;
import io.kojan.workflow.model.Workflow;
import io.kojan.workflow.model.WorkflowBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/// @author Mikolaj Izdebski
class WorkflowFactory {
    public Workflow createFromPlan(
            Platform platform,
            Platform testPlatform,
            Plan plan,
            Subject subject,
            boolean includeProvision) {
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        TaskFactory taskFactory = new TaskFactory(workflowBuilder);
        Map<String, Task> srpms = new LinkedHashMap<>();
        Map<String, Task> checkouts = new LinkedHashMap<>();

        Task gatherRepo = null;
        LinkedList<Task> repos = new LinkedList<>();
        if (!plan.getPhases().isEmpty()) {
            Task gather = taskFactory.createGatherTask("platform", platform);
            gatherRepo =
                    taskFactory.createRepoTask("platform-repo", Collections.singletonList(gather));
            repos.add(gatherRepo);
        }
        Map<String, Task> rpmsByName = new LinkedHashMap<>();

        for (Phase phase : plan.getPhases()) {
            List<Task> rpms = new ArrayList<>();

            for (String component : phase.getComponents()) {
                Task srpm = srpms.get(component);
                if (srpm == null) {
                    SubjectComponent componentSubject = subject.getSubjectComponent(component);
                    Task checkout = taskFactory.createCheckoutTask(componentSubject);
                    checkouts.put(component, checkout);
                    srpm =
                            taskFactory.createSrpmTask(
                                    component, checkout, gatherRepo, plan.getMacros());
                    srpms.put(component, srpm);
                }

                Task rpm =
                        taskFactory.createRpmTask(
                                component,
                                phase.getName(),
                                srpm,
                                repos,
                                plan.getMacros(),
                                phase.getMacros());
                rpms.add(rpm);
                rpmsByName.put(component, rpm);
            }

            Task repo = taskFactory.createRepoTask(phase.getName() + "-repo", rpms);
            repos.addFirst(repo);
        }

        Task compose =
                taskFactory.createRepoTask(
                        "compose",
                        Stream.concat(srpms.values().stream(), rpmsByName.values().stream())
                                .toList());

        if (includeProvision) {
            Task gatherTest = taskFactory.createGatherTask("test-platform", testPlatform);
            Task gatherTestRepo =
                    taskFactory.createRepoTask(
                            "test-platform-repo", Collections.singletonList(gatherTest));
            taskFactory.createProvisionTask(gatherTestRepo, compose);
        }

        return workflowBuilder.build();
    }
}
