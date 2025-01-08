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
package io.kojan.mbici;

import io.kojan.mbici.execute.KubeExecuteCommand;
import io.kojan.mbici.execute.LocalExecuteCommand;
import io.kojan.mbici.generate.GenerateCommand;
import io.kojan.mbici.report.ReportCommand;
import io.kojan.mbici.subject.LocalSubjectCommand;
import io.kojan.mbici.workspace.ConfigCommand;
import io.kojan.mbici.workspace.InitCommand;
import io.kojan.mbici.workspace.LogCommand;
import io.kojan.mbici.workspace.RunCommand;
import io.kojan.mbici.workspace.StatusCommand;
import io.kojan.mbici.workspace.TestCommand;
import io.kojan.mbici.workspace.ValidateCommand;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;

/**
 * @author Mikolaj Izdebski
 */
@Command(
        name = "mbi",
        subcommands = {
            InitCommand.class,
            ConfigCommand.class,
            RunCommand.class,
            LogCommand.class,
            ValidateCommand.class,
            TestCommand.class,
            StatusCommand.class,
            LocalSubjectCommand.class,
            GenerateCommand.class,
            LocalExecuteCommand.class,
            KubeExecuteCommand.class,
            ReportCommand.class,
        },
        mixinStandardHelpOptions = true,
        versionProvider = Main.class)
public class Main implements IVersionProvider {
    public static void main(String... args) {
        int exitCode =
                new CommandLine(new Main())
                        .registerConverter(Path.class, arg -> Path.of(arg).toAbsolutePath())
                        .execute(args);
        System.exit(exitCode);
    }

    @Override
    public String[] getVersion() throws Exception {
        String ver = "UNKNOWN";
        try (InputStream is =
                Main.class.getResourceAsStream(
                        "/META-INF/maven/io.kojan/mbici-workflow/pom.properties")) {
            if (is != null) {
                Properties properties = new Properties();
                properties.load(is);
                ver = properties.getProperty("version");
            }
        }
        return new String[] {
            "MBI version " + ver,
            "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
            "OS: ${os.name} ${os.version} ${os.arch}"
        };
    }
}
