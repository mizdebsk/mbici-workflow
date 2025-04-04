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
package io.kojan.mbici.model;

import io.kojan.xml.Attribute;
import io.kojan.xml.Entity;
import io.kojan.xml.Relationship;
import java.util.Collections;
import java.util.List;

/// @author Mikolaj Izdebski
public class Phase {
    private final String name;
    private final List<String> components;
    private final List<Macro> macros;

    public Phase(String name, List<String> components, List<Macro> macros) {
        this.name = name;
        this.components = Collections.unmodifiableList(components);
        this.macros = Collections.unmodifiableList(macros);
    }

    public String getName() {
        return name;
    }

    public List<String> getComponents() {
        return components;
    }

    public List<Macro> getMacros() {
        return macros;
    }

    static final Entity<Phase, PhaseBuilder> ENTITY =
            Entity.of(
                    "phase",
                    PhaseBuilder::new,
                    Attribute.of("name", Phase::getName, PhaseBuilder::setName),
                    Attribute.ofMulti(
                            "component", Phase::getComponents, PhaseBuilder::addComponent),
                    Relationship.of(Macro.ENTITY, Phase::getMacros, PhaseBuilder::addMacro));
}
