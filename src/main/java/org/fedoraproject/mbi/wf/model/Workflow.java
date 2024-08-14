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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.fedoraproject.mbi.xml.Entity;

/**
 * @author Mikolaj Izdebski
 */
public class Workflow {
    private final List<Task> tasks;
    private final List<Result> results;

    public Workflow(List<Task> tasks, List<Result> results) {
        this.tasks = Collections.unmodifiableList(new ArrayList<>(tasks));
        this.results = Collections.unmodifiableList(new ArrayList<>(results));
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<Result> getResults() {
        return results;
    }

    static final Entity<Workflow, WorkflowBuilder> ENTITY = new Entity<>("workflow", WorkflowBuilder::new);
    static {
        ENTITY.addRelationship(Task.ENTITY, Workflow::getTasks, WorkflowBuilder::addTask);
        ENTITY.addRelationship(Result.ENTITY, Workflow::getResults, WorkflowBuilder::addResult);
    }

    public static Workflow readFromXML(Path path) throws IOException, XMLStreamException {
        return ENTITY.readFromXML(path);
    }

    public void writeToXML(Path path) throws IOException, XMLStreamException {
        ENTITY.writeToXML(path, this);
    }
}
