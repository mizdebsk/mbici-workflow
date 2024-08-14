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

import io.kojan.xml.Builder;

/**
 * @author Mikolaj Izdebski
 */
public class SubjectComponentBuilder implements Builder<SubjectComponent> {
    private String name;
    private String scm;
    private String commit;
    private String lookaside;

    public void setName(String name) {
        this.name = name;
    }

    public void setScm(String scm) {
        this.scm = scm;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public void setLookaside(String lookaside) {
        this.lookaside = lookaside;
    }

    @Override
    public SubjectComponent build() {
        return new SubjectComponent(name, scm, commit, lookaside);
    }
}
