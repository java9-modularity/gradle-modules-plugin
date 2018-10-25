package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;

public class TestModuleOptions extends ModuleOptions {

    private boolean runOnClasspath;

    public TestModuleOptions(Project project) {
        super(project);
    }

    public boolean isRunOnClasspath() {
        return runOnClasspath;
    }

    public void setRunOnClasspath(boolean runOnClasspath) {
        this.runOnClasspath = runOnClasspath;
    }

}
