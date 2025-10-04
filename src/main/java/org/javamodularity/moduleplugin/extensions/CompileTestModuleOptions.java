package org.javamodularity.moduleplugin.extensions;

import org.gradle.api.Project;

public class CompileTestModuleOptions extends ModuleOptions {

    private boolean compileOnClasspath;

    public CompileTestModuleOptions(Project project) {
        super(project);
    }

    @Deprecated(since = "1.8.16", forRemoval = true)
    public boolean isCompileOnClasspath() {
        return compileOnClasspath;
    }

    public boolean getCompileOnClasspath() {
        return compileOnClasspath;
    }

    public void setCompileOnClasspath(boolean compileOnClasspath) {
        this.compileOnClasspath = compileOnClasspath;
    }
}
