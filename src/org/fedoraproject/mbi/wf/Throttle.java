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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.fedoraproject.mbi.wf.handler.CheckoutTaskHandler;
import org.fedoraproject.mbi.wf.handler.RpmTaskHandler;
import org.fedoraproject.mbi.wf.handler.SrpmTaskHandler;
import org.fedoraproject.mbi.wf.model.Task;

/**
 * @author Mikolaj Izdebski
 */
public class Throttle
{
    private final Map<String, Semaphore> semaphores = new LinkedHashMap<>();

    public Throttle( int maxCheckout, int maxSrpm, int maxRpm )
    {
        semaphores.put( CheckoutTaskHandler.class.getName(), new Semaphore( maxCheckout ) );
        semaphores.put( RpmTaskHandler.class.getName(), new Semaphore( maxRpm ) );
        semaphores.put( SrpmTaskHandler.class.getName(), new Semaphore( maxSrpm ) );
    }

    public void acquireCapacity( Task task )
    {
        Semaphore sema = semaphores.get( task.getHandler() );
        if ( sema != null )
        {
            sema.acquireUninterruptibly();
        }
    }

    public void releaseCapacity( Task task )
    {
        Semaphore sema = semaphores.get( task.getHandler() );
        if ( sema != null )
        {
            sema.release();
        }
    }
}
