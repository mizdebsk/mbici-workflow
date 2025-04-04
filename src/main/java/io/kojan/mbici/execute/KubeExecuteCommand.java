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
package io.kojan.mbici.execute;

import io.kojan.mbici.Main;
import io.kojan.mbici.tasks.Kubernetes;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/// @author Mikolaj Izdebski
@Command(
        name = "kube-exec",
        description = "Execute Workflow on Kubernetes cluster.",
        mixinStandardHelpOptions = true,
        versionProvider = Main.class)
public class KubeExecuteCommand extends AbstractExecuteCommand {
    @Option(
            names = {"--namespace"},
            description = "Kubernetes Namespace.")
    private String namespace;

    @Option(
            names = {"--container-image"},
            description = "Container image to use for running all commands.")
    private String containerImage = "quay.io/mizdebsk/mock:prod";

    @Option(
            names = {"--cache-volume-claim-name"},
            description = "Name of PersistentVolumeClaim to use for cache.")
    private String cacheVolumeClaimName = "mbici-cache";

    @Option(
            names = {"--result-volume-claim-name"},
            description = "Name of PersistentVolumeClaim to use for results.")
    private String resultVolumeClaimName = "mbici-result";

    @Option(
            names = {"--pod-running-timeout"},
            description = "Pod running timeout.")
    private String podRunningTimeout = "30m";

    @Option(
            names = {"--srpm-cpu-request"},
            description = "Requested CPU for running SRPM Pods.")
    private String srpmCpuRequest = "500m";

    @Option(
            names = {"--srpm-cpu-limit"},
            description = "Max CPU for running SRPM Pods.")
    private String srpmCpuLimit = "2";

    @Option(
            names = {"--rpm-cpu-request"},
            description = "Requested CPU for running RPM Pods.")
    private String rpmCpuRequest = "1";

    @Option(
            names = {"--rpm-cpu-limit"},
            description = "Max CPU for running RPM Pods.")
    private String rpmCpuLimit = "4";

    @Option(
            names = {"--srpm-memory-request"},
            description = "Requested memory for running SRPM Pods.")
    private String srpmMemoryRequest = "256Mi";

    @Option(
            names = {"--srpm-memory-limit"},
            description = "Max memory for running SRPM Pods.")
    private String srpmMemoryLimit = "1Gi";

    @Option(
            names = {"--rpm-memory-request"},
            description = "Requested memory for running RPM Pods.")
    private String rpmMemoryRequest = "1Gi";

    @Option(
            names = {"--rpm-memory-limit"},
            description = "Max memory for running RPM Pods.")
    private String rpmMemoryLimit = "6Gi";

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getContainerImage() {
        return containerImage;
    }

    public void setContainerImage(String containerImage) {
        this.containerImage = containerImage;
    }

    public String getCacheVolumeClaimName() {
        return cacheVolumeClaimName;
    }

    public void setCacheVolumeClaimName(String cacheVolumeClaimName) {
        this.cacheVolumeClaimName = cacheVolumeClaimName;
    }

    public String getResultVolumeClaimName() {
        return resultVolumeClaimName;
    }

    public void setResultVolumeClaimName(String resultVolumeClaimName) {
        this.resultVolumeClaimName = resultVolumeClaimName;
    }

    public String getPodRunningTimeout() {
        return podRunningTimeout;
    }

    public void setPodRunningTimeout(String podRunningTimeout) {
        this.podRunningTimeout = podRunningTimeout;
    }

    public String getSrpmCpuRequest() {
        return srpmCpuRequest;
    }

    public void setSrpmCpuRequest(String srpmCpuRequest) {
        this.srpmCpuRequest = srpmCpuRequest;
    }

    public String getSrpmCpuLimit() {
        return srpmCpuLimit;
    }

    public void setSrpmCpuLimit(String srpmCpuLimit) {
        this.srpmCpuLimit = srpmCpuLimit;
    }

    public String getRpmCpuRequest() {
        return rpmCpuRequest;
    }

    public void setRpmCpuRequest(String rpmCpuRequest) {
        this.rpmCpuRequest = rpmCpuRequest;
    }

    public String getRpmCpuLimit() {
        return rpmCpuLimit;
    }

    public void setRpmCpuLimit(String rpmCpuLimit) {
        this.rpmCpuLimit = rpmCpuLimit;
    }

    public String getSrpmMemoryRequest() {
        return srpmMemoryRequest;
    }

    public void setSrpmMemoryRequest(String srpmMemoryRequest) {
        this.srpmMemoryRequest = srpmMemoryRequest;
    }

    public String getSrpmMemoryLimit() {
        return srpmMemoryLimit;
    }

    public void setSrpmMemoryLimit(String srpmMemoryLimit) {
        this.srpmMemoryLimit = srpmMemoryLimit;
    }

    public String getRpmMemoryRequest() {
        return rpmMemoryRequest;
    }

    public void setRpmMemoryRequest(String rpmMemoryRequest) {
        this.rpmMemoryRequest = rpmMemoryRequest;
    }

    public String getRpmMemoryLimit() {
        return rpmMemoryLimit;
    }

    public void setRpmMemoryLimit(String rpmMemoryLimit) {
        this.rpmMemoryLimit = rpmMemoryLimit;
    }

    protected void initialize() {
        Kubernetes kubernetes =
                new Kubernetes(
                        getCacheManager(),
                        namespace,
                        containerImage,
                        cacheVolumeClaimName,
                        resultVolumeClaimName,
                        podRunningTimeout,
                        srpmCpuRequest,
                        srpmCpuLimit,
                        rpmCpuRequest,
                        rpmCpuLimit,
                        srpmMemoryRequest,
                        srpmMemoryLimit,
                        rpmMemoryRequest,
                        rpmMemoryLimit);
        io.kojan.mbici.tasks.Command.kubernetes = kubernetes;
    }
}
