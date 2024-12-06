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

    public Plan getPlan() {
        return plan;
    }

    public Platform getPlatform() {
        return platform;
    }

    private YamlConf(Plan plan, Platform platform) {
        this.plan = plan;
        this.platform = platform;
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
        PlatformBuilder platformBuilder = new PlatformBuilder();

        List<String> phases =
                conf.keySet().stream()
                        .filter(k -> !k.endsWith("-macros"))
                        .filter(k -> !k.equals("macros"))
                        .filter(k -> !k.equals("platform"))
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

        Map<String, Object> platform = (Map<String, Object>) conf.get("platform");

        List<String> repos = platform.keySet().stream().filter(k -> !k.equals("packages")).toList();
        for (String repo : repos) {
            platformBuilder.addRepo(new Repo(repo, (String) platform.get(repo)));
        }

        List<String> packages = (List<String>) platform.get("packages");
        for (String pkg : packages) {
            platformBuilder.addPackage(pkg);
        }

        return new YamlConf(planBuilder.build(), platformBuilder.build());
    }
}
