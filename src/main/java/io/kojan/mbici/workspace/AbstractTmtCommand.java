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
package io.kojan.mbici.workspace;

import io.kojan.mbici.AbstractCommand;
import io.kojan.workflow.model.Result;
import io.kojan.workflow.model.Workflow;
import io.kojan.xml.XMLException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.Option;

public abstract class AbstractTmtCommand extends AbstractCommand {

    @Option(
            names = {"-e", "--environment"},
            description = "Environment to pass to tmt, in format key=val.")
    protected final List<String> environment = new ArrayList<>();

    @Option(
            names = {"-c", "--context"},
            description = "Context to pass to tmt, in format key=val.")
    protected final List<String> context = new ArrayList<>();

    public static Path findComposeOrAbort(Workspace ws) throws IOException, XMLException {
        WorkspaceConfig c = ws.getConfig();
        Workflow wf = Workflow.readFromXML(c.getWorkflowPath());
        for (Result result : wf.getResults()) {
            if (result.getTaskId().equals("compose")) {
                return c.getResultDir().resolve("compose").resolve(result.getId()).resolve("repo");
            }
        }
        throw new RuntimeException("This command requires a compose");
    }
}
