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

import io.kojan.mbici.cache.CacheManager;
import io.kojan.workflow.TaskExecutionContext;
import io.kojan.workflow.TaskTermination;
import io.kojan.workflow.model.Task;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mikolaj Izdebski
 */
public class Kubernetes {
    private final CacheManager cacheManager;
    private final String namespace;
    private final String containerImage;
    private final String cacheVolumeClaimName;
    private final String resultVolumeClaimName;
    private final String podRunningTimeout;
    private final String srpmCpuRequest;
    private final String srpmCpuLimit;
    private final String rpmCpuRequest;
    private final String rpmCpuLimit;
    private final String srpmMemoryRequest;
    private final String srpmMemoryLimit;
    private final String rpmMemoryRequest;
    private final String rpmMemoryLimit;

    public Kubernetes(
            CacheManager cacheManager,
            String namespace,
            String containerImage,
            String cacheVolumeClaimName,
            String resultVolumeClaimName,
            String podRunningTimeout,
            String srpmCpuRequest,
            String srpmCpuLimit,
            String rpmCpuRequest,
            String rpmCpuLimit,
            String srpmMemoryRequest,
            String srpmMemoryLimit,
            String rpmMemoryRequest,
            String rpmMemoryLimit) {
        this.cacheManager = cacheManager;
        this.namespace = namespace;
        this.containerImage = containerImage;
        this.cacheVolumeClaimName = cacheVolumeClaimName;
        this.resultVolumeClaimName = resultVolumeClaimName;
        this.podRunningTimeout = podRunningTimeout;
        this.srpmCpuRequest = srpmCpuRequest;
        this.srpmCpuLimit = srpmCpuLimit;
        this.rpmCpuRequest = rpmCpuRequest;
        this.rpmCpuLimit = rpmCpuLimit;
        this.srpmMemoryRequest = srpmMemoryRequest;
        this.srpmMemoryLimit = srpmMemoryLimit;
        this.rpmMemoryRequest = rpmMemoryRequest;
        this.rpmMemoryLimit = rpmMemoryLimit;
    }

    public List<String> wrapCommand(TaskExecutionContext context, List<String> command)
            throws TaskTermination {
        Task task = context.getTask();

        // Kubernetes doesn't allow underscore in Pod names
        String podName = task.getId().replace('_', '-');

        String cpuRequest = task.getHandler().contains("Srpm") ? srpmCpuRequest : rpmCpuRequest;
        String cpuLimit = task.getHandler().contains("Srpm") ? srpmCpuLimit : rpmCpuLimit;
        String memRequest =
                task.getHandler().contains("Srpm") ? srpmMemoryRequest : rpmMemoryRequest;
        String memLimit = task.getHandler().contains("Srpm") ? srpmMemoryLimit : rpmMemoryLimit;

        StringBuilder pod = new StringBuilder();
        pod.append("{");
        pod.append("  \"spec\": {");
        pod.append("    \"containers\": [");
        pod.append("      {");
        pod.append("        \"name\": \"main\",");
        pod.append("        \"image\": \"").append(containerImage).append("\",");
        pod.append("        \"imagePullPolicy\": \"IfNotPresent\",");
        pod.append("        \"command\": [");
        var it = command.iterator();
        pod.append("\"").append(it.next()).append("\"");
        while (it.hasNext()) {
            pod.append(", \"").append(it.next()).append("\"");
        }
        pod.append("        ],");
        pod.append("        \"securityContext\": {");
        pod.append("          \"privileged\": true,");
        pod.append("          \"runAsUser\": 18611");
        pod.append("        },");
        pod.append("        \"volumeMounts\": [");
        pod.append("          {");
        pod.append("            \"name\": \"cache\",");
        pod.append("            \"mountPath\": \"")
                .append(cacheManager.getCacheRootDir().toString())
                .append("\"");
        pod.append("          },");
        pod.append("          {");
        pod.append("            \"name\": \"result\",");
        pod.append("            \"mountPath\": \"")
                .append(cacheManager.getResultRootDir().toString())
                .append("\"");
        pod.append("          },");
        pod.append("          {");
        pod.append("            \"name\": \"work\",");
        pod.append("            \"mountPath\": \"")
                .append(cacheManager.getWorkRootDir().toString())
                .append("\"");
        pod.append("          }");
        pod.append("        ],");
        pod.append("        \"resources\": {");
        pod.append("          \"requests\": {");
        pod.append("            \"cpu\": \"").append(cpuRequest).append("\",");
        pod.append("            \"memory\": \"").append(memRequest).append("\"");
        pod.append("          },");
        pod.append("          \"limits\": {");
        pod.append("            \"cpu\": \"").append(cpuLimit).append("\",");
        pod.append("            \"memory\": \"").append(memLimit).append("\"");
        pod.append("          }");
        pod.append("        }");
        pod.append("      }");
        pod.append("    ],");
        pod.append("    \"volumes\": [");
        pod.append("      {");
        pod.append("        \"name\": \"cache\",");
        pod.append("        \"persistentVolumeClaim\": {");
        pod.append("          \"claimName\": \"").append(cacheVolumeClaimName).append("\"");
        pod.append("        }");
        pod.append("      },");
        pod.append("      {");
        pod.append("        \"name\": \"result\",");
        pod.append("        \"persistentVolumeClaim\": {");
        pod.append("          \"claimName\": \"").append(resultVolumeClaimName).append("\"");
        pod.append("        }");
        pod.append("      },");
        pod.append("      {");
        pod.append("        \"name\": \"work\",");
        pod.append("        \"emptyDir\": {");
        pod.append("          \"medium\": \"Memory\",");
        pod.append("          \"sizeLimit\": \"1Gi\"");
        pod.append("        }");
        pod.append("      }");
        pod.append("    ]");
        pod.append("  }");
        pod.append("}");

        List<String> args = new ArrayList<>();
        args.add("kubectl");
        args.add("run");
        args.add(podName);
        args.add("--namespace=" + namespace);
        args.add("--quiet");
        args.add("--attach");
        args.add("--pod-running-timeout=" + podRunningTimeout);
        args.add("--rm");
        args.add("--restart=Never");
        args.add("--image=" + containerImage);
        args.add("--overrides=" + pod.toString());

        return args;
    }
}
