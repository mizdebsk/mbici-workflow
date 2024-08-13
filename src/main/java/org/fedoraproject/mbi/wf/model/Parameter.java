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

import org.fedoraproject.mbi.xml.Entity;

/**
 * @author Mikolaj Izdebski
 */
public class Parameter
{
    private final String name;

    private final String value;

    public Parameter( String name, String value )
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }

    static final Entity<Parameter, ParameterBuilder> ENTITY = new Entity<>( "parameter", ParameterBuilder::new );
    static
    {
        ENTITY.addAttribute( "name", Parameter::getName, ParameterBuilder::setName );
        ENTITY.addAttribute( "value", Parameter::getValue, ParameterBuilder::setValue );
    }
}
