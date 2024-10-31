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
package io.kojan.mbici.model;

import io.kojan.xml.Entity;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLStreamException;

/**
 * @author Mikolaj Izdebski
 */
public class Plan {
    private final List<Phase> phases;
    private final List<Macro> macros;

    public Plan(List<Phase> phases, List<Macro> macros) {
        this.phases = Collections.unmodifiableList(phases);
        this.macros = Collections.unmodifiableList(macros);
    }

    public List<Phase> getPhases() {
        return phases;
    }

    public List<Macro> getMacros() {
        return macros;
    }

    static final Entity<Plan, PlanBuilder> ENTITY = new Entity<>("plan", PlanBuilder::new);

    static {
        ENTITY.addRelationship(Phase.ENTITY, Plan::getPhases, PlanBuilder::addPhase);
        ENTITY.addRelationship(Macro.ENTITY, Plan::getMacros, PlanBuilder::addMacro);
    }

    public static Plan readFromXML(Path path) throws IOException, XMLStreamException {
        return ENTITY.readFromXML(path);
    }

    public void writeToXML(Path path) throws IOException, XMLStreamException {
        ENTITY.writeToXML(path, this);
    }
}
