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

import java.util.Collections;
import java.util.List;

import org.fedoraproject.mbi.xml.Entity;

/**
 * @author Mikolaj Izdebski
 */
public class Task {
    private final String id;
    private final String handler;
    private final List<String> dependencies;
    private final List<Parameter> parameters;

    public Task(String id, String handler, List<String> dependencies, List<Parameter> parameters) {
        this.id = id;
        this.handler = handler;
        this.dependencies = Collections.unmodifiableList(dependencies);
        this.parameters = Collections.unmodifiableList(parameters);
    }

    public Task(Task descriptor) {
        this.id = descriptor.getId();
        this.handler = descriptor.getHandler();
        this.dependencies = descriptor.getDependencies();
        this.parameters = descriptor.getParameters();
    }

    public String getId() {
        return id;
    }

    public String getHandler() {
        return handler;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "Task(" + id + ")";
    }

    static final Entity<Task, TaskBuilder> ENTITY = new Entity<>("task", TaskBuilder::new);
    static {
        ENTITY.addAttribute("id", Task::getId, TaskBuilder::setId);
        ENTITY.addAttribute("handler", Task::getHandler, TaskBuilder::setHandler);
        ENTITY.addMultiAttribute("dependency", Task::getDependencies, TaskBuilder::addDependency);
        ENTITY.addRelationship(Parameter.ENTITY, Task::getParameters, TaskBuilder::addParameter);
    }
}
