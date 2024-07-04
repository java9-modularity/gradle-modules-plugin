package org.javamodularity.moduleplugin.extensions;

import org.gradle.api.Project;

public class RunModuleOptions extends RuntimeModuleOptions {

    private Boolean createCommandLineArgumentFile = false;

    public Boolean getCreateCommandLineArgumentFile() {
        return createCommandLineArgumentFile;
    }

    public void setCreateCommandLineArgumentFile(Boolean createCommandLineArgumentFile) {
        this.createCommandLineArgumentFile = createCommandLineArgumentFile;
    }

    public RunModuleOptions(Project project) {
        super(project);
    }
}
