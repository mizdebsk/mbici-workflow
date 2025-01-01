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

import io.kojan.xml.Builder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mikolaj Izdebski
 */
public class PlanBuilder implements Builder<Plan> {
    private final List<Phase> phases = new ArrayList<>();
    private final List<Macro> macros = new ArrayList<>();

    public void addPhase(Phase phase) {
        phases.add(phase);
    }

    public void addMacro(Macro macro) {
        macros.add(macro);
    }

    @Override
    public Plan build() {
        return new Plan(phases, macros);
    }
}
