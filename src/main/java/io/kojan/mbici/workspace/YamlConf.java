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
package io.kojan.mbici.workspace;

import io.kojan.mbici.model.Macro;
import io.kojan.mbici.model.PhaseBuilder;
import io.kojan.mbici.model.Plan;
import io.kojan.mbici.model.PlanBuilder;
import io.kojan.mbici.model.Platform;
import io.kojan.mbici.model.PlatformBuilder;
import io.kojan.mbici.model.Repo;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class YamlConf {
    private final Plan plan;
    private final Platform platform;
    private final Platform testPlatform;

    public Plan getPlan() {
        return plan;
    }

    public Platform getPlatform() {
        return platform;
    }

    public Platform getTestPlatform() {
        return testPlatform;
    }

    private YamlConf(Plan plan, Platform platform, Platform testPlatform) {
        this.plan = plan;
        this.platform = platform;
        this.testPlatform = testPlatform;
    }

    @SuppressWarnings("unchecked")
    private static Platform loadPlatform(Map<String, Object> platform) {
        PlatformBuilder platformBuilder = new PlatformBuilder();

        if (platform != null) {
            List<String> repos =
                    platform.keySet().stream().filter(k -> !k.equals("packages")).toList();
            for (String repo : repos) {
                platformBuilder.addRepo(new Repo(repo, (String) platform.get(repo)));
            }

            List<String> packages = (List<String>) platform.getOrDefault("packages", List.of());
            for (String pkg : packages) {
                platformBuilder.addPackage(pkg);
            }
        }

        return platformBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public static YamlConf load(Path yamlPath) throws IOException {

        Yaml yaml = new Yaml();
        Object obj;
        try (Reader reader = Files.newBufferedReader(yamlPath)) {
            obj = yaml.load(reader);
        }
        Map<String, Object> conf = (Map<String, Object>) obj;

        PlanBuilder planBuilder = new PlanBuilder();

        List<String> phases =
                conf.keySet().stream()
                        .filter(k -> !k.endsWith("-macros"))
                        .filter(k -> !k.equals("macros"))
                        .filter(k -> !k.equals("platform"))
                        .filter(k -> !k.equals("test-platform"))
                        .toList();

        for (String phase : phases) {
            PhaseBuilder phaseBuilder = new PhaseBuilder();
            phaseBuilder.setName(phase);
            List<String> comps = (List<String>) conf.getOrDefault(phase, List.of());
            for (String comp : comps) {
                phaseBuilder.addComponent(comp);
            }
            Map<String, Object> macros =
                    (Map<String, Object>) conf.getOrDefault(phase + "-macros", Map.of());
            macros.forEach((key, val) -> phaseBuilder.addMacro(new Macro(key, val.toString())));
            planBuilder.addPhase(phaseBuilder.build());
        }

        Map<String, Object> macros = (Map<String, Object>) conf.getOrDefault("macros", Map.of());
        macros.forEach((key, val) -> planBuilder.addMacro(new Macro(key, val.toString())));

        return new YamlConf(
                planBuilder.build(),
                loadPlatform((Map<String, Object>) conf.get("platform")),
                loadPlatform((Map<String, Object>) conf.get("test-platform")));
    }
}
