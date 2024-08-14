/*-
 * Copyright (c) 2024 Red Hat, Inc.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.kojan.mbici.model.Subject;
import io.kojan.mbici.model.SubjectComponent;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "local-subject", description = "generate Subject from local dist-git repos", mixinStandardHelpOptions = true)
public class LocalSubjectCommand extends AbstractSubjectCommand {

    @Option(names = {"-S",
            "--scm"}, required = true, description = "path to directory containing dist-git repositories")
    protected Path scmPath;

    @Option(names = {"-r", "--ref"}, description = "git ref to use in each dist-git repository")
    protected String ref = "rawhide";

    private String resolveRef(Path repo, String ref) throws InterruptedException, IOException {
        List<String> command = Arrays.asList("git", "-C", repo.toString(), "rev-parse", ref);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectInput(Redirect.PIPE);
        pb.redirectOutput(Redirect.PIPE);
        pb.redirectError(Redirect.INHERIT);

        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        Process process = pb.start();
        process.getInputStream().transferTo(bis);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("git rev-parse failed with exit code " + exitCode + " for repo " + repo);
        }

        return new String(bis.toByteArray()).strip();
    }

    @Override
    protected Subject generateSubject(Set<String> components) throws Exception {
        List<SubjectComponent> subjComps = new ArrayList<>();
        for (String component : components) {
            Path repo = scmPath.resolve(component);
            String commit = resolveRef(repo, ref);
            subjComps.add(new SubjectComponent(component, repo.toString(), commit, lookaside + "/" + component));
        }
        return new Subject(subjComps);
    }

}
