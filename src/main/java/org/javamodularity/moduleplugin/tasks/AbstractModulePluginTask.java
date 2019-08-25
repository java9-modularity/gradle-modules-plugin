package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.javamodularity.moduleplugin.JavaProjectHelper;

abstract class AbstractModulePluginTask {

    protected final Project project;

    AbstractModulePluginTask(Project project) {
        this.project = project;
    }

    protected final JavaProjectHelper helper() {
        return new JavaProjectHelper(project);
    }

    protected final MergeClassesHelper mergeClassesHelper() {
        return new MergeClassesHelper(project);
    }
}
