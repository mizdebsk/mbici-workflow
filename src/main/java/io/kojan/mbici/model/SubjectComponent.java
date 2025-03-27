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

/// @author Mikolaj Izdebski
public class SubjectComponent {
    private final String name;
    private final String scm;
    private final String commit;
    private final String lookaside;

    public SubjectComponent(String name, String scm, String commit, String lookaside) {
        this.name = name;
        this.scm = scm;
        this.commit = commit;
        this.lookaside = lookaside;
    }

    public String getName() {
        return name;
    }

    public String getScm() {
        return scm;
    }

    public String getCommit() {
        return commit;
    }

    public String getLookaside() {
        return lookaside;
    }

    static final Entity<SubjectComponent, SubjectComponentBuilder> ENTITY =
            Entity.of(
                    "component",
                    SubjectComponentBuilder::new,
                    Attribute.of(
                            "name", SubjectComponent::getName, SubjectComponentBuilder::setName),
                    Attribute.of("scm", SubjectComponent::getScm, SubjectComponentBuilder::setScm),
                    Attribute.of(
                            "commit",
                            SubjectComponent::getCommit,
                            SubjectComponentBuilder::setCommit),
                    Attribute.of(
                            "lookaside",
                            SubjectComponent::getLookaside,
                            SubjectComponentBuilder::setLookaside));
}
