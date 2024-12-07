/*-
 * Copyright (c) 2024 Red Hat, Inc.
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
package io.kojan.mbici.workspace;

import java.nio.file.Path;

public class WorkspaceConfig {

    private Path subjectPath;
    private Path workflowPath;
    private Path planPath;
    private Path platformPath;
    private Path resultDir;
    private Path cacheDir;
    private Path workDir;
    private Path linkDir;
    private Path reportDir;
    private Path composeDir;
    private String lookaside;
    private Path scmDir;
    private String scmRef;
    private int maxCheckoutTasks;
    private int maxSrpmTasks;
    private int maxRpmTasks;

    public Path getSubjectPath() {
        return subjectPath;
    }

    public void setSubjectPath(Path subjectPath) {
        this.subjectPath = subjectPath;
    }

    public Path getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(Path workflowPath) {
        this.workflowPath = workflowPath;
    }

    public Path getPlanPath() {
        return planPath;
    }

    public void setPlanPath(Path planPath) {
        this.planPath = planPath;
    }

    public Path getPlatformPath() {
        return platformPath;
    }

    public void setPlatformPath(Path platformPath) {
        this.platformPath = platformPath;
    }

    public Path getResultDir() {
        return resultDir;
    }

    public void setResultDir(Path resultDir) {
        this.resultDir = resultDir;
    }

    public Path getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(Path cacheDir) {
        this.cacheDir = cacheDir;
    }

    public Path getWorkDir() {
        return workDir;
    }

    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    public Path getLinkDir() {
        return linkDir;
    }

    public void setLinkDir(Path linkDir) {
        this.linkDir = linkDir;
    }

    public Path getReportDir() {
        return reportDir;
    }

    public void setReportDir(Path reportDir) {
        this.reportDir = reportDir;
    }

    public Path getComposeDir() {
        return composeDir;
    }

    public void setComposeDir(Path composeDir) {
        this.composeDir = composeDir;
    }

    public String getLookaside() {
        return lookaside;
    }

    public void setLookaside(String lookaside) {
        this.lookaside = lookaside;
    }

    public Path getScmDir() {
        return scmDir;
    }

    public void setScmDir(Path scmDir) {
        this.scmDir = scmDir;
    }

    public String getScmRef() {
        return scmRef;
    }

    public void setScmRef(String scmRef) {
        this.scmRef = scmRef;
    }

    public int getMaxCheckoutTasks() {
        return maxCheckoutTasks;
    }

    public void setMaxCheckoutTasks(int maxCheckoutTasks) {
        this.maxCheckoutTasks = maxCheckoutTasks;
    }

    public int getMaxSrpmTasks() {
        return maxSrpmTasks;
    }

    public void setMaxSrpmTasks(int maxSrpmTasks) {
        this.maxSrpmTasks = maxSrpmTasks;
    }

    public int getMaxRpmTasks() {
        return maxRpmTasks;
    }

    public void setMaxRpmTasks(int maxRpmTasks) {
        this.maxRpmTasks = maxRpmTasks;
    }
}
