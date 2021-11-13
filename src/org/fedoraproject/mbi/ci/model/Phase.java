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
package org.fedoraproject.mbi.ci.model;

import java.util.Collections;
import java.util.List;

import org.fedoraproject.mbi.xml.Entity;

/**
 * @author Mikolaj Izdebski
 */
public class Phase
{
    private final String name;

    private final List<String> components;

    private final List<Macro> macros;

    public Phase( String name, List<String> components, List<Macro> macros )
    {
        this.name = name;
        this.components = Collections.unmodifiableList( components );
        this.macros = Collections.unmodifiableList( macros );
    }

    public String getName()
    {
        return name;
    }

    public List<String> getComponents()
    {
        return components;
    }

    public List<Macro> getMacros()
    {
        return macros;
    }

    static final Entity<Phase, PhaseBuilder> ENTITY = new Entity<>( "phase", PhaseBuilder::new );
    static
    {
        ENTITY.addAttribute( "name", Phase::getName, PhaseBuilder::setName );
        ENTITY.addMultiAttribute( "component", Phase::getComponents, PhaseBuilder::addComponent );
        ENTITY.addRelationship( Macro.ENTITY, Phase::getMacros, PhaseBuilder::addMacro );
    }
}
