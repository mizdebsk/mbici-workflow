/*-
 * Copyright (c) 2024-2025 Red Hat, Inc.
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
package io.kojan.mbici.subject;

import io.kojan.mbici.AbstractCommand;
import io.kojan.mbici.model.Phase;
import io.kojan.mbici.model.Plan;
import io.kojan.mbici.model.Subject;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import picocli.CommandLine.Option;

abstract class AbstractSubjectCommand extends AbstractCommand {
    @Option(
            names = {"-s", "--subject"},
            required = true,
            description = "Where to store generated Subject.")
    private Path subjectPath;

    @Option(
            names = {"-m", "--plan"},
            required = true,
            description = "Path to a Build Plan in XML format.")
    private Path planPath;

    @Option(
            names = {"-L", "--lookaside"},
            description = "Lookaside cache base URL.")
    protected String lookaside = "https://src.fedoraproject.org/lookaside/pkgs/rpms";

    public Path getSubjectPath() {
        return subjectPath;
    }

    public void setSubjectPath(Path subjectPath) {
        this.subjectPath = subjectPath;
    }

    public Path getPlanPath() {
        return planPath;
    }

    public void setPlanPath(Path planPath) {
        this.planPath = planPath;
    }

    public String getLookaside() {
        return lookaside;
    }

    public void setLookaside(String lookaside) {
        this.lookaside = lookaside;
    }

    protected abstract Subject generateSubject(Set<String> components) throws Exception;

    @Override
    public Integer call() throws Exception {
        Plan plan = Plan.readFromXML(planPath);
        Set<String> components = new LinkedHashSet<>();
        for (Phase phase : plan.getPhases()) {
            for (String component : phase.getComponents()) {
                components.add(component);
            }
        }
        Subject subject = generateSubject(components);
        subject.writeToXML(subjectPath);
        return 0;
    }
}
