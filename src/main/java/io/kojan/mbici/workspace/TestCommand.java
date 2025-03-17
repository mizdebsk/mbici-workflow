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

import io.kojan.mbici.Main;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = "test",
        description = "Run tmt test plan on built packages.",
        mixinStandardHelpOptions = true,
        versionProvider = Main.class)
public class TestCommand extends AbstractTmtCommand {

    @Parameters(index = "0", description = "Name of tmt test plan to run.")
    private String testPlan;

    @Override
    protected String getTestPlan() {
        return testPlan;
    }

    @Override
    protected boolean requiresGuest() {
        return true;
    }
}
