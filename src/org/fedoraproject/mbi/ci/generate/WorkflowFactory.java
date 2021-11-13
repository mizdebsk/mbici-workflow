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
package org.fedoraproject.mbi.ci.generate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.fedoraproject.mbi.ci.model.Phase;
import org.fedoraproject.mbi.ci.model.Plan;
import org.fedoraproject.mbi.ci.model.Platform;
import org.fedoraproject.mbi.ci.model.Subject;
import org.fedoraproject.mbi.ci.model.SubjectComponent;
import org.fedoraproject.mbi.wf.model.Task;
import org.fedoraproject.mbi.wf.model.Workflow;
import org.fedoraproject.mbi.wf.model.WorkflowBuilder;

/**
 * @author Mikolaj Izdebski
 */
class WorkflowFactory
{
    public Workflow createFromPlan( Platform platform, Plan plan, Subject subject )
    {
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        TaskFactory taskFactory = new TaskFactory( workflowBuilder );
        Map<String, Task> srpms = new LinkedHashMap<>();

        Task gather = taskFactory.createGatherTask( platform );

        LinkedList<Task> repos = new LinkedList<>();
        repos.add( gather );

        for ( Phase phase : plan.getPhases() )
        {
            List<Task> rpms = new ArrayList<>();

            for ( String component : phase.getComponents() )
            {
                Task srpm = srpms.get( component );
                if ( srpm == null )
                {
                    SubjectComponent componentSubject = subject.getSubjectComponent( component );
                    Task checkout = taskFactory.createCheckoutTask( componentSubject );
                    srpm = taskFactory.createSrpmTask( component, checkout, gather );
                    srpms.put( component, srpm );
                }

                Task rpm = taskFactory.createRpmTask( component, phase.getName(), srpm, repos, plan.getMacros(),
                                                      phase.getMacros() );
                rpms.add( rpm );
            }

            Task repo = taskFactory.createRepoTask( phase.getName(), rpms );
            repos.addFirst( repo );
        }

        return workflowBuilder.build();
    }
}
