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
public class Repo {
    private final String name;
    private final String url;

    public Repo(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    static final Entity<Repo, RepoBuilder> ENTITY = new Entity<>("repo", RepoBuilder::new);

    static {
        ENTITY.addAttribute("name", Repo::getName, RepoBuilder::setName);
        ENTITY.addAttribute("url", Repo::getUrl, RepoBuilder::setUrl);
    }
}
