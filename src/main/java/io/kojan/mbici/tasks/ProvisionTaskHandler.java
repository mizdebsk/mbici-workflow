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

import io.kojan.workflow.TaskExecutionContext;
import io.kojan.workflow.TaskTermination;
import io.kojan.workflow.model.Task;
import java.io.IOException;

/**
 * @author Mikolaj Izdebski
 */
public class ProvisionTaskHandler extends AbstractTaskHandler {

    private static ProvisionTaskHandler instance;

    private static synchronized void setInstance(ProvisionTaskHandler inst) {
        instance = inst;
    }

    public static ProvisionTaskHandler getInstance() {
        return instance;
    }

    public ProvisionTaskHandler(Task task) {
        setInstance(this);
    }

    private Guest guest;

    public synchronized Guest getGuest() {
        return guest;
    }

    private synchronized void setGuest(Guest guest) {
        this.guest = guest;
    }

    @Override
    public void handleTask(TaskExecutionContext context) throws TaskTermination {
        try {
            Guest guest = new Guest(context.getWorkDir());
            setGuest(guest);
            guest.runSshServer(context);
        } catch (IOException e) {
            TaskTermination.error("I/O exception when running SSH server");
        }
        TaskTermination.error("Provision ended unexpectedly");
    }
}
