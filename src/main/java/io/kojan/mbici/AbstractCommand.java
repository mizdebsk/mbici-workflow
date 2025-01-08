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
package io.kojan.mbici;

import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.ColorScheme;

public abstract class AbstractCommand implements Callable<Integer> {
    protected final ColorScheme cs = Help.defaultColorScheme(Ansi.AUTO);

    public void error(String msg) {
        System.err.println(cs.errorText(msg));
    }

    public void success(String msg) {
        System.err.println(cs.apply(msg, List.of(Style.fg_green)));
    }

    public void info(String msg) {
        System.err.println(msg);
    }
}
