package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;

public class TestModuleOptions {
    private boolean runOnClasspath;

    public TestModuleOptions(Project project) {
    }

    public boolean isRunOnClasspath() {
        return runOnClasspath;
    }

    public void setRunOnClasspath(boolean runOnClasspath) {
        this.runOnClasspath = runOnClasspath;
    }
}
