/*-
 * Copyright (c) 2022-2025 Red Hat, Inc.
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
package io.kojan.mbici.report;

import io.kojan.mbici.model.Subject;

/**
 * @author Mikolaj Izdebski
 */
public class SubjectReport extends Report {
    private final Subject subject;

    public SubjectReport(Subject subject) {
        this.subject = subject;
    }

    @Override
    public void body() {
        header("Test subject");
        para(
                "Test subject is a concrete set of dist-git repository commits which ",
                "are used as sources for building RPM packages.");
        para(
                "For each component that is part of ",
                link("plan.html", "plan"),
                ", test subject ",
                "specifies dist-git URL and exact commit hash, as well as URL of ",
                "lookaside cache used to download source blobs from.");
        para(
                "Machine-readable information about test subject in XML format can be found in ",
                link("subject.xml", "subject.xml"),
                ". Human-readable information is included below.");

        for (var c : subject.getComponentOverrides()) {
            subtitle(c.getName());
            add("<p>SCM URL: ", c.getScm());
            add("<br/>SCM commit: ", c.getCommit());
            add("<br/>Lookaside URL: ", c.getLookaside());
            add("</p>");
        }

        footer();
    }
}
