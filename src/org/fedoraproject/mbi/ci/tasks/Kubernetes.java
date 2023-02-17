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
package org.fedoraproject.mbi.ci.tasks;

import java.util.ArrayList;
import java.util.List;

import org.fedoraproject.mbi.wf.CacheManager;
import org.fedoraproject.mbi.wf.TaskExecution;
import org.fedoraproject.mbi.wf.TaskTermination;
import org.fedoraproject.mbi.wf.model.Task;

/**
 * @author Mikolaj Izdebski
 */
class Kubernetes
{
    private static final String CONTAINER_IMAGE = "quay.io/mizdebsk/mock:prod";

    private static final String CACHE_VOLUME_CLAIM_NAME = "mbici-cache";

    private static final String RESULT_VOLUME_CLAIM_NAME = "mbici-result";

    private static final String POD_RUNNING_TIMEOUT = "30m";

    private static final String SRPM_CPU_REQUEST = "500m";

    private static final String SRPM_CPU_LIMIT = "2";

    private static final String RPM_CPU_REQUEST = "1";

    private static final String RPM_CPU_LIMIT = "4";

    private static final String SRPM_MEM_REQUEST = "256Mi";

    private static final String SRPM_MEM_LIMIT = "1Gi";

    private static final String RPM_MEM_REQUEST = "1Gi";

    private static final String RPM_MEM_LIMIT = "4Gi";

    private final String namespace;

    public Kubernetes( String namespace )
    {
        this.namespace = namespace;
    }

    public List<String> wrapCommand( TaskExecution taskExecution, List<String> command )
        throws TaskTermination
    {
        Task task = taskExecution.getTask();
        CacheManager cacheManager = taskExecution.getCacheManager();

        // Kubernetes doesn't allow underscore in Pod names
        String podName = task.getId().replace( '_', '-' );

        String cpuRequest = task.getHandler().contains( "Srpm" ) ? SRPM_CPU_REQUEST : RPM_CPU_REQUEST;
        String cpuLimit = task.getHandler().contains( "Srpm" ) ? SRPM_CPU_LIMIT : RPM_CPU_LIMIT;
        String memRequest = task.getHandler().contains( "Srpm" ) ? SRPM_MEM_REQUEST : RPM_MEM_REQUEST;
        String memLimit = task.getHandler().contains( "Srpm" ) ? SRPM_MEM_LIMIT : RPM_MEM_LIMIT;

        StringBuilder pod = new StringBuilder();
        pod.append( "{" );
        pod.append( "  \"spec\": {" );
        pod.append( "    \"containers\": [" );
        pod.append( "      {" );
        pod.append( "        \"name\": \"main\"," );
        pod.append( "        \"image\": \"" ).append( CONTAINER_IMAGE ).append( "\"," );
        pod.append( "        \"imagePullPolicy\": \"IfNotPresent\"," );
        pod.append( "        \"command\": [" );
        var it = command.iterator();
        pod.append( "\"" ).append( it.next() ).append( "\"" );
        while ( it.hasNext() )
        {
            pod.append( ", \"" ).append( it.next() ).append( "\"" );
        }
        pod.append( "        ]," );
        pod.append( "        \"securityContext\": {" );
        pod.append( "          \"privileged\": true," );
        pod.append( "          \"runAsUser\": 18611" );
        pod.append( "        }," );
        pod.append( "        \"volumeMounts\": [" );
        pod.append( "          {" );
        pod.append( "            \"name\": \"cache\"," );
        pod.append( "            \"mountPath\": \"" ).append( cacheManager.getCacheRootDir().toString() ).append( "\"" );
        pod.append( "          }," );
        pod.append( "          {" );
        pod.append( "            \"name\": \"result\"," );
        pod.append( "            \"mountPath\": \"" ).append( cacheManager.getResultRootDir().toString() ).append( "\"" );
        pod.append( "          }," );
        pod.append( "          {" );
        pod.append( "            \"name\": \"work\"," );
        pod.append( "            \"mountPath\": \"" ).append( cacheManager.getWorkRootDir().toString() ).append( "\"" );
        pod.append( "          }" );
        pod.append( "        ]," );
        pod.append( "        \"resources\": {" );
        pod.append( "          \"requests\": {" );
        pod.append( "            \"cpu\": \"" ).append( cpuRequest ).append( "\"," );
        pod.append( "            \"memory\": \"" ).append( memRequest ).append( "\"" );
        pod.append( "          }," );
        pod.append( "          \"limits\": {" );
        pod.append( "            \"cpu\": \"" ).append( cpuLimit ).append( "\"," );
        pod.append( "            \"memory\": \"" ).append( memLimit ).append( "\"" );
        pod.append( "          }" );
        pod.append( "        }" );
        pod.append( "      }" );
        pod.append( "    ]," );
        pod.append( "    \"volumes\": [" );
        pod.append( "      {" );
        pod.append( "        \"name\": \"cache\"," );
        pod.append( "        \"persistentVolumeClaim\": {" );
        pod.append( "          \"claimName\": \"" ).append( CACHE_VOLUME_CLAIM_NAME ).append( "\"" );
        pod.append( "        }" );
        pod.append( "      }," );
        pod.append( "      {" );
        pod.append( "        \"name\": \"result\"," );
        pod.append( "        \"persistentVolumeClaim\": {" );
        pod.append( "          \"claimName\": \"" ).append( RESULT_VOLUME_CLAIM_NAME ).append( "\"" );
        pod.append( "        }" );
        pod.append( "      }," );
        pod.append( "      {" );
        pod.append( "        \"name\": \"work\"," );
        pod.append( "        \"emptyDir\": {" );
        pod.append( "          \"medium\": \"Memory\"," );
        pod.append( "          \"sizeLimit\": \"1Gi\"" );
        pod.append( "        }" );
        pod.append( "      }" );
        pod.append( "    ]" );
        pod.append( "  }" );
        pod.append( "}" );

        List<String> args = new ArrayList<>();
        args.add( "kubectl" );
        args.add( "run" );
        args.add( podName );
        args.add( "--namespace=" + namespace );
        args.add( "--quiet" );
        args.add( "--attach" );
        args.add( "--wait" );
        args.add( "--pod-running-timeout=" + POD_RUNNING_TIMEOUT );
        args.add( "--rm" );
        args.add( "--restart=Never" );
        args.add( "--image=" + CONTAINER_IMAGE );
        args.add( "--overrides=" + pod.toString() );

        return args;
    }

}
