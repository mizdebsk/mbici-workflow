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
package io.kojan.mbici.cache;

import io.kojan.workflow.TaskStorage;
import io.kojan.workflow.TaskTermination;
import io.kojan.workflow.model.Task;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

/// @author Mikolaj Izdebski
public class CacheManager implements TaskStorage {
    private final Path resultRootDir;
    private final Path cacheRootDir;
    private final Path workRootDir;

    public CacheManager(Path resultRootDir, Path cacheRootDir, Path workRootDir)
            throws IOException {
        this.resultRootDir = resultRootDir;
        this.cacheRootDir = cacheRootDir;
        this.workRootDir = workRootDir;
    }

    public Path getResultRootDir() {
        return resultRootDir;
    }

    public Path getCacheRootDir() {
        return cacheRootDir;
    }

    public Path getWorkRootDir() {
        return workRootDir;
    }

    public Path getResultDir(Task task, String resultId) {
        return resultRootDir.resolve(task.getId()).resolve(resultId);
    }

    public Path getWorkDir(Task task, String resultId) {
        return workRootDir.resolve(task.getId()).resolve(resultId);
    }

    public Path getDistGit(String key) throws IOException {
        Path distgitCacheDir = cacheRootDir.resolve("distgit");
        Files.createDirectories(distgitCacheDir);
        return distgitCacheDir.resolve(key);
    }

    public Path getLookaside(String key) throws IOException {
        Path lookasideCacheDir = cacheRootDir.resolve("lookaside");
        Files.createDirectories(lookasideCacheDir);
        return lookasideCacheDir.resolve(key);
    }

    public Path createPending(String key) throws TaskTermination, IOException {
        Path cachePendingDir = cacheRootDir.resolve("pending");
        Files.createDirectories(cachePendingDir);
        return Files.createTempDirectory(
                cachePendingDir,
                key,
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x")));
    }
}
