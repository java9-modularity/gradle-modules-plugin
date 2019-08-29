package org.javamodularity.moduleplugin.extensions;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.javamodularity.moduleplugin.JavaProjectHelper;
import org.javamodularity.moduleplugin.internal.TaskOption;
import org.javamodularity.moduleplugin.internal.StreamHelper;
import org.javamodularity.moduleplugin.tasks.MergeClassesHelper;

import java.util.*;
import java.util.stream.Stream;

public abstract class ModuleOptions {
    private static final Logger LOGGER = Logging.getLogger(ModuleOptions.class);

    private final Project project;

    private List<String> addModules = new ArrayList<>();
    private Map<String, String> addReads = new LinkedHashMap<>();
    private Map<String, String> addExports = new LinkedHashMap<>();
    private Map<String, String> addOpens = new LinkedHashMap<>();

    protected ModuleOptions(Project project) {
        this.project = project;
    }

    public List<String> getAddModules() {
        return addModules;
    }

    public void setAddModules(List<String> addModules) {
        this.addModules = addModules;
    }

    public Map<String, String> getAddReads() {
        return addReads;
    }

    public void setAddReads(Map<String, String> addReads) {
        this.addReads = addReads;
    }

    public Map<String, String> getAddExports() {
        return addExports;
    }

    public void setAddExports(Map<String, String> addExports) {
        this.addExports = addExports;
    }

    public Map<String, String> getAddOpens() {
        return addOpens;
    }

    public void setAddOpens(Map<String, String> addOpens) {
        this.addOpens = addOpens;
    }

    //region MODULE TASK OPTION
    public void mutateArgs(List<String> args) {
        buildFullOptionStream().forEach(o -> o.mutateArgs(args));
    }

    public Stream<TaskOption> buildFullOptionStream() {
        LOGGER.debug("Updating module '{}' with...", helper().moduleName());
        return StreamHelper.concat(
                addModulesOption().stream(),
                addReadsOptionStream(),
                addExportsOptionStream(),
                addOpensOptionStream()
        ).peek(option -> LOGGER.debug("  {} {}", option.getFlag(), option.getValue()));
    }

    private Optional<TaskOption> addModulesOption() {
        if (addModules.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new TaskOption("--add-modules", String.join(",", addModules)));
    }

    private Stream<TaskOption> addReadsOptionStream() {
        return buildOptionStream("--add-reads", addReads);
    }

    private Stream<TaskOption> addExportsOptionStream() {
        return buildOptionStream("--add-exports", addExports);
    }

    private Stream<TaskOption> addOpensOptionStream() {
        return buildOptionStream("--add-opens", addOpens);
    }

    private Stream<TaskOption> buildOptionStream(String flag, Map<String, String> map) {
        if (map.isEmpty()) {
            return Stream.empty();
        }

        return map.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .map(value -> new TaskOption(flag, value));
    }
    //endregion

    protected final JavaProjectHelper helper() {
        return new JavaProjectHelper(project);
    }

    protected final MergeClassesHelper mergeClassesHelper() {
        return new MergeClassesHelper(project);
    }
}
