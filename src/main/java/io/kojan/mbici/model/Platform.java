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

import io.kojan.xml.Attribute;
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
public class Platform {
    private final List<Repo> repos;
    private final List<String> packages;

    public Platform(List<Repo> repos, List<String> packages) {
        this.repos = Collections.unmodifiableList(repos);
        this.packages = Collections.unmodifiableList(packages);
    }

    public List<Repo> getRepos() {
        return repos;
    }

    public List<String> getPackages() {
        return packages;
    }

    static final Entity<Platform, PlatformBuilder> ENTITY =
            Entity.of(
                    "platform",
                    PlatformBuilder::new,
                    Relationship.of(Repo.ENTITY, Platform::getRepos, PlatformBuilder::addRepo),
                    Attribute.ofMulti(
                            "package", Platform::getPackages, PlatformBuilder::addPackage));

    public static Platform readFromXML(Path path) throws IOException, XMLException {
        return ENTITY.readFromXML(path);
    }

    public void writeToXML(Path path) throws IOException, XMLException {
        ENTITY.writeToXML(path, this);
    }
}
