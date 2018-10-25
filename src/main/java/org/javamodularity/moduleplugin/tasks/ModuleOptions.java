package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

public class ModuleOptions {
    private List<String> addModules = new ArrayList<>();

    public ModuleOptions(Project project) {}

    public List<String> getAddModules() {
        return addModules;
    }

    public void setAddModules(List<String> addModules) {
        this.addModules = addModules;
    }
}
