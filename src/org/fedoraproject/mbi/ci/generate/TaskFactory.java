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

import java.util.List;

import org.fedoraproject.mbi.ci.model.Macro;
import org.fedoraproject.mbi.ci.model.Platform;
import org.fedoraproject.mbi.ci.model.Repo;
import org.fedoraproject.mbi.ci.model.SubjectComponent;
import org.fedoraproject.mbi.wf.handler.CheckoutTaskHandler;
import org.fedoraproject.mbi.wf.handler.GatherTaskHandler;
import org.fedoraproject.mbi.wf.handler.RepoTaskHandler;
import org.fedoraproject.mbi.wf.handler.RpmTaskHandler;
import org.fedoraproject.mbi.wf.handler.SrpmTaskHandler;
import org.fedoraproject.mbi.wf.handler.ValidateTaskHandler;
import org.fedoraproject.mbi.wf.model.Task;
import org.fedoraproject.mbi.wf.model.TaskBuilder;
import org.fedoraproject.mbi.wf.model.WorkflowBuilder;

/**
 * @author Mikolaj Izdebski
 */
class TaskFactory
{
    private static final String RPM_HANDLER = RpmTaskHandler.class.getName();

    private static final String SRPM_HANDLER = SrpmTaskHandler.class.getName();

    private static final String GATHER_HANDLER = GatherTaskHandler.class.getName();

    private static final String CHECKOUT_HANDLER = CheckoutTaskHandler.class.getName();

    private static final String REPO_HANDLER = RepoTaskHandler.class.getName();

    private static final String VALIDATE_HANDLER = ValidateTaskHandler.class.getName();

    private final WorkflowBuilder workflowBuilder;

    public TaskFactory( WorkflowBuilder workflowBuilder )
    {
        this.workflowBuilder = workflowBuilder;
    }

    public Task createGatherTask( Platform platform )
    {
        TaskBuilder task = new TaskBuilder();
        task.setId( "platform" );
        task.setHandler( GATHER_HANDLER );

        for ( Repo repo : platform.getRepos() )
        {
            task.addParameter( "repo-" + repo.getName(), repo.getUrl() );
        }

        int i = 0;
        for ( String packageName : platform.getPackages() )
        {
            task.addParameter( "package-" + ++i, packageName );
        }

        Task taskDescriptor = task.build();
        workflowBuilder.addTask( taskDescriptor );
        return taskDescriptor;
    }

    public Task createRepoTask( String phase, List<Task> rpms )
    {
        TaskBuilder task = new TaskBuilder();
        task.setId( phase + "-repo" );
        task.setHandler( REPO_HANDLER );

        for ( Task rpm : rpms )
        {
            task.addDependency( rpm.getId() );
        }

        Task taskDescriptor = task.build();
        workflowBuilder.addTask( taskDescriptor );
        return taskDescriptor;
    }

    public Task createCheckoutTask( SubjectComponent cs )
    {
        TaskBuilder task = new TaskBuilder();
        task.setId( cs.getName() + "-checkout" );
        task.setHandler( CHECKOUT_HANDLER );
        task.addParameter( "scm", cs.getScm() );
        task.addParameter( "commit", cs.getCommit() );
        task.addParameter( "lookaside", cs.getLookaside() );

        Task taskDescriptor = task.build();
        workflowBuilder.addTask( taskDescriptor );
        return taskDescriptor;
    }

    public Task createSrpmTask( String component, Task checkout, Task repo )
    {
        TaskBuilder task = new TaskBuilder();
        task.setId( component + "-srpm" );
        task.setHandler( SRPM_HANDLER );
        task.addDependency( checkout.getId() );
        task.addDependency( repo.getId() );

        Task taskDescriptor = task.build();
        workflowBuilder.addTask( taskDescriptor );
        return taskDescriptor;
    }

    public Task createRpmTask( String component, String phase, Task srpm, List<Task> repos, List<Macro> planMacros,
                               List<Macro> phaseMacros )
    {
        TaskBuilder task = new TaskBuilder();
        task.setId( component + "-" + phase + "-rpm" );
        task.setHandler( RPM_HANDLER );
        task.addDependency( srpm.getId() );
        for ( Task repo : repos )
        {
            task.addDependency( repo.getId() );
        }

        for ( Macro macro : planMacros )
        {
            task.addParameter( macro.getName(), macro.getValue() );
        }

        for ( Macro macro : phaseMacros )
        {
            task.addParameter( macro.getName(), macro.getValue() );
        }

        Task taskDescriptor = task.build();
        workflowBuilder.addTask( taskDescriptor );
        return taskDescriptor;
    }

    public Task createValidateTask( String component, String phase, Task checkout, Task srpm, Task rpm )
    {
        TaskBuilder task = new TaskBuilder();
        task.setId( component + "-" + phase + "-validate" );
        task.setHandler( VALIDATE_HANDLER );
        task.addDependency( checkout.getId() );
        task.addDependency( srpm.getId() );
        task.addDependency( rpm.getId() );

        Task taskDescriptor = task.build();
        workflowBuilder.addTask( taskDescriptor );
        return taskDescriptor;
    }
}
