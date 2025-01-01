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

import io.kojan.xml.Entity;
import io.kojan.xml.Relationship;
import io.kojan.xml.XMLException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * @author Mikolaj Izdebski
 */
public class Subject {
    private final List<SubjectComponent> subjectComponents;

    public Subject(List<SubjectComponent> subjectComponents) {
        this.subjectComponents = Collections.unmodifiableList(subjectComponents);
    }

    public List<SubjectComponent> getComponentOverrides() {
        return subjectComponents;
    }

    public SubjectComponent getSubjectComponent(String component) {
        for (SubjectComponent override : subjectComponents) {
            if (override.getName().equals(component)) {
                return override;
            }
        }

        return null;
    }

    static final Entity<Subject, SubjectBuilder> ENTITY =
            Entity.of(
                    "subject",
                    SubjectBuilder::new,
                    Relationship.of(
                            SubjectComponent.ENTITY,
                            Subject::getComponentOverrides,
                            SubjectBuilder::addSubjectComponent));

    public static Subject readFromXML(Path path) throws IOException, XMLException {
        return ENTITY.readFromXML(path);
    }

    public void writeToXML(Path path) throws IOException, XMLException {
        ENTITY.writeToXML(path, this);
    }
}
