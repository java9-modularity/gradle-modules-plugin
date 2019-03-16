package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModuleOptions {
    private static final Logger LOGGER = Logging.getLogger(ModuleOptions.class);

    private List<String> addModules = new ArrayList<>();
    private Map<String, String> addReads = new LinkedHashMap<>();
    private Map<String, String> addExports = new LinkedHashMap<>();
    private Map<String, String> addOpens = new LinkedHashMap<>();

    public ModuleOptions(Project project) {
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

    protected void mutateArgs(String moduleName, List<String> args) {
        if (!getAddModules().isEmpty()) {
            String addModules = String.join(",", getAddModules());
            args.add("--add-modules");
            args.add(addModules);
            LOGGER.debug("Adding modules '{}' to patch module {}...", addModules, moduleName);
        }

        mutateArgs(moduleName, args, getAddReads(), "--add-reads");
        mutateArgs(moduleName, args, getAddExports(), "--add-exports");
        mutateArgs(moduleName, args, getAddOpens(), "--add-opens");
    }

    private void mutateArgs(String moduleName, List<String> args, Map<String, String> src, String flag) {
        if (!src.isEmpty()) {
            LOGGER.debug("Updating module '{}' with...", moduleName);
            src.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .forEach(e -> {
                    LOGGER.debug("  {}", flag);
                    LOGGER.debug("  {}", e);
                    args.add(flag);
                    args.add(e);
                });
        }
    }
}
