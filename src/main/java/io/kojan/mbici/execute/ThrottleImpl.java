/*-
 * Copyright (c) 2021-2023 Red Hat, Inc.
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import io.kojan.mbici.tasks.CheckoutTaskHandler;
import io.kojan.mbici.tasks.RpmTaskHandler;
import io.kojan.mbici.tasks.SrpmTaskHandler;
import io.kojan.workflow.Throttle;
import io.kojan.workflow.model.Task;

/**
 * @author Mikolaj Izdebski
 */
class ThrottleImpl implements Throttle {
    private final Map<String, Semaphore> semaphores = new LinkedHashMap<>();

    public ThrottleImpl(int maxCheckout, int maxSrpm, int maxRpm) {
        semaphores.put(CheckoutTaskHandler.class.getName(), new Semaphore(maxCheckout));
        semaphores.put(RpmTaskHandler.class.getName(), new Semaphore(maxRpm));
        semaphores.put(SrpmTaskHandler.class.getName(), new Semaphore(maxSrpm));
    }

    @Override
    public void acquireCapacity(Task task) {
        Semaphore sema = semaphores.get(task.getHandler());
        if (sema != null) {
            sema.acquireUninterruptibly();
        }
    }

    @Override
    public void releaseCapacity(Task task) {
        Semaphore sema = semaphores.get(task.getHandler());
        if (sema != null) {
            sema.release();
        }
    }
}
