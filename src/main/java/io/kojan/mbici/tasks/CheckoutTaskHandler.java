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
package io.kojan.mbici.tasks;

import io.kojan.mbici.cache.ArtifactType;
import io.kojan.workflow.TaskExecutionContext;
import io.kojan.workflow.TaskTermination;
import io.kojan.workflow.model.Parameter;
import io.kojan.workflow.model.Task;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// @author Mikolaj Izdebski
public class CheckoutTaskHandler extends AbstractTaskHandler {
    private static final int GIT_TIMEOUT = 300;

    private final String scm;
    private final String commit;
    private final String lookaside;

    public CheckoutTaskHandler(Task task) {
        String scm = null;
        String commit = null;
        String lookaside = null;
        for (Parameter param : task.getParameters()) {
            switch (param.getName()) {
                case "scm":
                    scm = param.getValue();
                    break;
                case "commit":
                    commit = param.getValue();
                    break;
                case "lookaside":
                    lookaside = param.getValue();
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unknown checkout task parameter: " + param.getName());
            }
        }

        if (scm == null) {
            throw new IllegalArgumentException("Mandatory parameter scm was not provided");
        }
        if (commit == null) {
            throw new IllegalArgumentException("Mandatory parameter commit was not provided");
        }
        if (lookaside == null) {
            throw new IllegalArgumentException("Mandatory parameter lookaside was not provided");
        }

        this.scm = scm;
        this.commit = commit;
        this.lookaside = lookaside;
    }

    private void runGit(String logName, TaskExecutionContext context, String... args)
            throws TaskTermination {
        Command git = new Command("git");
        git.setName(logName);
        git.addArg("--git-dir", context.getWorkDir().resolve("git").toString());
        git.addArg(args);
        git.run(context, GIT_TIMEOUT);
    }

    public void handleTask0(TaskExecutionContext context) throws TaskTermination, IOException {
        Curl curl = new Curl(context);
        Path dgCache = getCacheManager().getDistGit(commit);

        Path artifact = context.addArtifact(ArtifactType.CHECKOUT, "checkout");
        try {
            Files.createSymbolicLink(artifact, dgCache);
        } catch (IOException e) {
            TaskTermination.error(
                    "I/O error when linking artifact " + artifact + ": " + e.getMessage());
        }

        if (Files.exists(dgCache)) {
            TaskTermination.success("Commit was found in dist-git cache");
            return;
        }
        Path workTree = getCacheManager().createPending("checkout-" + commit);
        runGit("git-init", context, "init", "--bare");
        runGit(
                "git-fetch",
                context,
                "-c",
                "http.version=HTTP/1.1",
                "remote",
                "add",
                "--fetch",
                "origin",
                scm);
        Files.createDirectories(workTree);
        runGit("git-reset", context, "--work-tree", workTree.toString(), "reset", "--hard", commit);
        for (String line : Files.readAllLines(workTree.resolve("sources"))) {
            Pattern pattern = Pattern.compile("^SHA512 \\(([^)]+)\\) = ([0-9a-f]{128})$");
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String fileName = matcher.group(1);
                String hash = matcher.group(2);
                Path lasCache = getCacheManager().getLookaside(hash);
                Path downloadPath = workTree.resolve(fileName);
                if (!Files.exists(lasCache)) {
                    String url = lookaside + "/" + fileName + "/sha512/" + hash + "/" + fileName;
                    curl.downloadFile(url, downloadPath);
                    Files.move(
                            downloadPath,
                            lasCache,
                            StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING);
                }
                Files.createLink(downloadPath, lasCache);
            }
        }

        try {
            Files.move(workTree, dgCache, StandardCopyOption.ATOMIC_MOVE);
        } catch (FileAlreadyExistsException e) {
            // Checkout was completed by a concurrent task, lets reuse its
            // results
            // TODO: remove workTree - don't leave garbage behind
            TaskTermination.success("Commit was found in dist-git cache");
            return;
        }

        TaskTermination.success("Commit was checked out from SCM");
    }

    @Override
    public void handleTask(TaskExecutionContext context) throws TaskTermination {
        try {
            handleTask0(context);
        } catch (IOException e) {
            TaskTermination.error("I/O error during checkout: " + e.getMessage());
        }
    }
}
