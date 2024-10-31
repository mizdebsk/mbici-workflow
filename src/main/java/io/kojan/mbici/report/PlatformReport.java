/*-
 * Copyright (c) 2022 Red Hat, Inc.
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

import io.kojan.mbici.model.Platform;

/**
 * @author Mikolaj Izdebski
 */
public class PlatformReport extends Report {
    private final Platform platform;

    public PlatformReport(Platform platform) {
        this.platform = platform;
    }

    @Override
    public void body() {
        header("Platform");

        para(
                "Platform is a minimal subset of RPM-based operating system that is ",
                "enough to build RPM packages listed in ",
                link("subject.html", "test subject"),
                ".");
        para(
                "Platform is defined by one or more YUM repositories with a number ",
                "of RPM packages that are taken from these repositories, along with ",
                "their runtime dependencies.");
        para(
                "Platform is only a small subset of the whole OS for two reasons: to ",
                "improve build performance and to prevent new dependencies on OS ",
                "packages from getting in unnoticed.");
        para(
                "Machine-readable information about platform in XML format can be found in ",
                link("platform.xml", "platform.xml"),
                ". Human-readable information is included below.");

        subtitle("YUM Repositories");
        list(
                "",
                "",
                platform.getRepos(),
                repo -> "Name: " + repo.getName() + "<br/>URL: " + repo.getUrl());

        subtitle("Packages");
        list("", "", platform.getPackages(), pkg -> pkg);

        footer();
    }
}
