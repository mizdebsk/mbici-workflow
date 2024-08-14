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

import io.kojan.xml.Builder;

/**
 * @author Mikolaj Izdebski
 */
public class TaskBuilder implements Builder<Task> {
    private String id;
    private String handler;
    private final List<String> dependencies = new ArrayList<>();
    private final List<Parameter> parameters = new ArrayList<>();

    public void setId(String id) {
        this.id = id;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public void addDependency(String dependency) {
        dependencies.add(dependency);
    }

    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    public void addParameter(String name, String value) {
        addParameter(new Parameter(name, value));
    }

    @Override
    public Task build() {
        return new Task(id, handler, dependencies, parameters);
    }
}
