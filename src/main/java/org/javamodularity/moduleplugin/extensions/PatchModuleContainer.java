package org.javamodularity.moduleplugin.extensions;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.javamodularity.moduleplugin.JavaProjectHelper;
import org.javamodularity.moduleplugin.internal.PatchModuleMutator;
import org.javamodularity.moduleplugin.internal.TaskOption;

import java.io.File;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PatchModuleContainer {
    private final Map<String, List<String>> jarToModuleNameMap = new LinkedHashMap<>();
    private final Map<String, List<String>> dirToModuleNameMap = new LinkedHashMap<>();

    public static PatchModuleContainer copyOf(PatchModuleContainer container) {
        var copyContainer = new PatchModuleContainer();
        copyContainer.jarToModuleNameMap.putAll(container.jarToModuleNameMap);
        copyContainer.dirToModuleNameMap.putAll(container.dirToModuleNameMap);
        return copyContainer;
    }

    public List<String> patchedJarNames() {
        return jarToModuleNameMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
    }

    public void addJar(String moduleName, String jarName) {
        var jarNames = jarToModuleNameMap.computeIfAbsent(moduleName, key -> new ArrayList<>());
        jarNames.add(jarName);
    }

    public void addDir(String moduleName, String dirPath) {
        var dirPaths = dirToModuleNameMap.computeIfAbsent(moduleName, key -> new ArrayList<>());
        dirPaths.add(dirPath);
    }

    public Optional<TaskOption> buildModulePathOption(FileCollection classpath) {
        String modulePath = classpath.filter(this::isUnpatched).getAsPath();
        if (modulePath.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new TaskOption("--module-path", modulePath));
    }

    public boolean isUnpatched(File jar) {
        return jarToModuleNameMap.entrySet().stream()
                .noneMatch(entry -> entry.getValue().contains(jar.getName()));
    }

    public PatchModuleMutator mutator(FileCollection classpath) {
        return new PatchModuleMutator(this, classpath);
    }

    public PatchModuleMutator mutator(UnaryOperator<String> jarNameResolver) {
        return new PatchModuleMutator(this, jarNameResolver);
    }

    public Stream<Map.Entry<String, List<String>>> jarPatchedModulesStream() {
        return jarToModuleNameMap.entrySet().stream();
    }

    public Stream<Map.Entry<String, List<String>>> dirPatchedModulesStream() {
        return dirToModuleNameMap.entrySet().stream();
    }

    public static void configure(Project project) {
        PatchModuleContainer container = new JavaProjectHelper(project).modularityExtension().optionContainer().getPatchModuleContainer();
        project.afterEvaluate(container::configureAfterEvaluate);
    }

    private void configureAfterEvaluate(Project project) {
        PatchModuleExtension patchModuleExtension = new JavaProjectHelper(project).extension(PatchModuleExtension.class);
        patchModuleExtension.getConfig().forEach( config -> {
            String[] tokens = config.split("=");
            addJar(tokens[0].trim(), tokens[1].trim());
        });
    }
}
