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
package org.fedoraproject.mbi.ci;

import org.fedoraproject.mbi.ci.generate.GenerateCommand;
import org.fedoraproject.mbi.ci.report.ReportCommand;
import org.fedoraproject.mbi.ci.run.RunCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * @author Mikolaj Izdebski
 */
@Command( name = "mbici-wf", subcommands = { //
    GenerateCommand.class, //
    RunCommand.class, //
    ReportCommand.class, //
}, mixinStandardHelpOptions = true )
public class Main
{
    public static void main( String... args )
    {
        int exitCode = new CommandLine( new Main() ).execute( args );
        System.exit( exitCode );
    }
}
