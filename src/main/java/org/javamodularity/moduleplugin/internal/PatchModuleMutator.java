package org.javamodularity.moduleplugin.internal;

import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.javamodularity.moduleplugin.extensions.PatchModuleContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PatchModuleMutator {
    private static final Logger LOGGER = Logging.getLogger(PatchModuleMutator.class);

    private final PatchModuleContainer patchModuleContainer;
    private final UnaryOperator<String> jarNameResolver;

    public PatchModuleMutator(PatchModuleContainer patchModuleContainer, FileCollection classpath) {
        this(patchModuleContainer,
                jarName -> classpath.filter(jar -> jar.getName().endsWith(jarName)).getAsPath());
    }

    public PatchModuleMutator(PatchModuleContainer patchModuleContainer,
                              UnaryOperator<String> jarNameResolver) {
        this.patchModuleContainer = patchModuleContainer;
        this.jarNameResolver = jarNameResolver;
    }

    public void mutateArgs(List<String> args) {
        taskOptionStream().forEach(option -> option.mutateArgs(args));
    }

    public Stream<TaskOption> taskOptionStream() {
        return getPatchMap().entrySet().stream()
                .map(entry -> buildTaskOption(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull);
    }

    private Map<String, List<String>> getPatchMap() {
        var patchMap = patchModuleContainer.jarPatchedModulesStream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> resolveJars(entry.getKey(), entry.getValue())
                ));
        patchModuleContainer.dirPatchedModulesStream().forEach(entry -> {
            var paths = patchMap.computeIfAbsent(entry.getKey(), key -> new ArrayList<>());
            paths.addAll(entry.getValue());
        });
        return patchMap;
    }

    private TaskOption buildTaskOption(String moduleName, List<String> paths) {
        if(paths.isEmpty()) return null;
        return new TaskOption("--patch-module", moduleName + "=" +
                paths.stream().collect(Collectors.joining(File.pathSeparator)));
    }

    private List<String> resolveJars(String moduleName, List<String> jarNames) {
        return jarNames.stream()
                .map(jarName -> getJarPath(moduleName, jarName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String getJarPath(String moduleName, String jarName) {
        String jarPath = jarNameResolver.apply(jarName);
        if (jarPath.isEmpty()) {
            LOGGER.warn("Skipped patching {} into {}", jarName, moduleName);
            return null;
        }
        return jarPath;
    }
}
