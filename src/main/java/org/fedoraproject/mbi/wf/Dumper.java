/*-
 * Copyright (c) 2021 Red Hat, Inc.
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
package org.fedoraproject.mbi.wf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.xml.stream.XMLStreamException;

import org.fedoraproject.mbi.wf.model.Workflow;

/**
 * @author Mikolaj Izdebski
 */
public class Dumper extends Thread {
    private final Path workflowPath;
    private Workflow queue;
    private boolean terminate;

    public Dumper(Path workflowPath) {
        this.workflowPath = workflowPath;
    }

    private synchronized Workflow peek() throws InterruptedException {
        while (queue == null && !terminate) {
            wait();
        }
        try {
            return queue;
        } finally {
            queue = null;
        }
    }

    public synchronized void terminate() {
        terminate = true;
        notify();
    }

    public synchronized void dumpEventually(Workflow wf) {
        queue = wf;
        notify();
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Workflow wf = peek();

                if (wf != null) {
                    Path tempPath = workflowPath.getParent().resolve("wf.xml.tmp");
                    wf.writeToXML(tempPath);
                    Files.move(tempPath, workflowPath, StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING);
                } else {
                    return;
                }
            }
        } catch (InterruptedException | IOException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}