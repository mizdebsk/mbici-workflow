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

import java.util.ArrayList;
import java.util.List;

import org.fedoraproject.mbi.xml.Builder;

/**
 * @author Mikolaj Izdebski
 */
public class WorkflowBuilder
    implements Builder<Workflow>
{
    private final List<Task> tasks = new ArrayList<>();

    private final List<Result> results = new ArrayList<>();

    public void addTask( Task task )
    {
        tasks.add( task );
    }

    public void addResult( Result result )
    {
        results.add( result );
    }

    @Override
    public Workflow build()
    {
        return new Workflow( tasks, results );
    }
}
