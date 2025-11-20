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
package io.kojan.mbici.execute;

import io.kojan.mbici.cache.CacheManager;
import io.kojan.mbici.tasks.AbstractTaskHandler;
import io.kojan.mbici.tasks.CheckoutTaskHandler;
import io.kojan.mbici.tasks.GatherTaskHandler;
import io.kojan.mbici.tasks.ProvisionTaskHandler;
import io.kojan.mbici.tasks.RepoTaskHandler;
import io.kojan.mbici.tasks.RpmTaskHandler;
import io.kojan.mbici.tasks.SrpmTaskHandler;
import io.kojan.workflow.TaskHandler;
import io.kojan.workflow.TaskHandlerFactory;
import io.kojan.workflow.model.Task;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class TaskHandlerFactoryImpl implements TaskHandlerFactory {
    private final Map<String, Function<Task, ? extends AbstractTaskHandler>> registry =
            new LinkedHashMap<>();
    private final CacheManager cacheManager;

    private void registerHandler(
            Class<? extends AbstractTaskHandler> cls,
            Function<Task, ? extends AbstractTaskHandler> ctor) {
        registry.put(cls.getCanonicalName(), ctor);
    }

    public TaskHandlerFactoryImpl(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        registerHandler(CheckoutTaskHandler.class, CheckoutTaskHandler::new);
        registerHandler(GatherTaskHandler.class, GatherTaskHandler::new);
        registerHandler(RepoTaskHandler.class, RepoTaskHandler::new);
        registerHandler(RpmTaskHandler.class, RpmTaskHandler::new);
        registerHandler(SrpmTaskHandler.class, SrpmTaskHandler::new);
        registerHandler(ProvisionTaskHandler.class, ProvisionTaskHandler::new);
    }

    @Override
    public TaskHandler createTaskHandler(Task task) {
        Function<Task, ? extends AbstractTaskHandler> ctor = registry.get(task.getHandler());
        if (ctor == null) {
            throw new IllegalArgumentException("Unsupported task handler: " + task.getHandler());
        }
        AbstractTaskHandler taskHandler = ctor.apply(task);
        taskHandler.setCacheManager(cacheManager);
        return taskHandler;
    }
}
