package org.javamodularity.moduleplugin.extensions;

import org.gradle.api.Project;

public class TestModuleOptions extends RuntimeModuleOptions {

    private boolean runOnClasspath;

    public TestModuleOptions(Project project) {
        super(project);
    }

    public boolean getRunOnClasspath() {
        return runOnClasspath;
    }

    public void setRunOnClasspath(boolean runOnClasspath) {
        this.runOnClasspath = runOnClasspath;
    }

}
