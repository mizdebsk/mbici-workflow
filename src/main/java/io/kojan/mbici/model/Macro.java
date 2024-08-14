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

/**
 * @author Mikolaj Izdebski
 */
public class Macro {
    private final String name;
    private final String value;

    public Macro(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    static final Entity<Macro, MacroBuilder> ENTITY = new Entity<>("macro", MacroBuilder::new);
    static {
        ENTITY.addAttribute("name", Macro::getName, MacroBuilder::setName);
        ENTITY.addAttribute("value", Macro::getValue, MacroBuilder::setValue);
    }
}
