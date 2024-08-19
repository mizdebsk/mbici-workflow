/*-
 * Copyright (c) 2021-2024 Red Hat, Inc.
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
package io.kojan.mbici;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.kojan.mbici.execute.KubeExecuteCommand;
import io.kojan.mbici.execute.LocalExecuteCommand;
import io.kojan.mbici.generate.GenerateCommand;
import io.kojan.mbici.report.ReportCommand;
import io.kojan.mbici.subject.LocalSubjectCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * @author Mikolaj Izdebski
 */
@Command(name = "mbici-wf", subcommands = { //
        LocalSubjectCommand.class, //
        GenerateCommand.class, //
        LocalExecuteCommand.class, //
        KubeExecuteCommand.class, //
        ReportCommand.class, //
}, mixinStandardHelpOptions = true)
public class Main {
    public static void main(String... args) {
        int exitCode = new CommandLine(new Main()) //
                .registerConverter(Path.class, arg -> Paths.get(arg).toAbsolutePath()) //
                .execute(args);
        System.exit(exitCode);
    }
}
