/*-
 * Copyright (c) 2021-2024 Red Hat, Inc.
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
package org.fedoraproject.mbi.ci.run;

import org.fedoraproject.mbi.ci.tasks.Kubernetes;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @author Mikolaj Izdebski
 */
@Command( name = "kube-run", description = "execute Workflow on Kubernetes cluster", mixinStandardHelpOptions = true )
public class KubeRunCommand
    extends AbstractRunCommand
{
    @Option( names = { "--namespace" }, description = "Kubernetes Namespace" )
    private String namespace;

    @Option( names = { "--container-image" }, description = "Container image to use for running all commands" )
    private String containerImage = "quay.io/mizdebsk/mock:prod";

    @Option( names = { "--cache-volume-claim-name" }, description = "Name of PersistentVolumeClaim to use for cache" )
    private String cacheVolumeClaimName = "mbici-cache";

    @Option( names = {
        "--result-volume-claim-name" }, description = "Name of PersistentVolumeClaim to use for results" )
    private String resultVolumeClaimName = "mbici-result";

    @Option( names = { "--pod-running-timeout" }, description = "Pod running timeout" )
    private String podRunningTimeout = "30m";

    @Option( names = { "--srpm-cpu-request" }, description = "Requested CPU for running SRPM Pods" )
    private String srpmCpuRequest = "500m";

    @Option( names = { "--srpm-cpu-limit" }, description = "Max CPU for running SRPM Pods" )
    private String srpmCpuLimit = "2";

    @Option( names = { "--rpm-cpu-request" }, description = "Requested CPU for running RPM Pods" )
    private String rpmCpuRequest = "1";

    @Option( names = { "--rpm-cpu-limit" }, description = "Max CPU for running RPM Pods" )
    private String rpmCpuLimit = "4";

    @Option( names = { "--srpm-memory-request" }, description = "Requested memory for running SRPM Pods" )
    private String srpmMemoryRequest = "256Mi";

    @Option( names = { "--srpm-memory-limit" }, description = "Max memory for running SRPM Pods" )
    private String srpmMemoryLimit = "1Gi";

    @Option( names = { "--rpm-memory-request" }, description = "Requested memory for running RPM Pods" )
    private String rpmMemoryRequest = "1Gi";

    @Option( names = { "--rpm-memory-limit" }, description = "Max memory for running RPM Pods" )
    private String rpmMemoryLimit = "6Gi";

    @Override
    public Integer call()
        throws Exception
    {
        Kubernetes kubernetes =
            new Kubernetes( namespace, containerImage, cacheVolumeClaimName, resultVolumeClaimName, podRunningTimeout,
                            srpmCpuRequest, srpmCpuLimit, rpmCpuRequest, rpmCpuLimit, srpmMemoryRequest,
                            srpmMemoryLimit, rpmMemoryRequest, rpmMemoryLimit );
        org.fedoraproject.mbi.ci.tasks.Command.kubernetes = kubernetes;
        return super.call();
    }
}
