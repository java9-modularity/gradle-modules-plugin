package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.JavaExec;
import org.javamodularity.moduleplugin.JavaProjectHelper;

import java.util.Objects;

abstract class AbstractExecutionMutator {

    private static final Logger LOGGER = Logging.getLogger(AbstractExecutionMutator.class);

    protected final JavaExec execTask;
    protected final Project project;

    AbstractExecutionMutator(JavaExec execTask, Project project) {
        this.execTask = execTask;
        this.project = project;
    }

    protected final String getMainClassName() {
        String mainClassName = Objects.requireNonNull(execTask.getMain());
        if (!mainClassName.contains("/")) {
            LOGGER.warn("No module was provided for main class, assuming the current module. Prefer providing 'mainClassName' in the following format: '$moduleName/a.b.Main'");
            return helper().moduleName() + "/" + mainClassName;
        }
        return mainClassName;
    }

    protected final JavaProjectHelper helper() {
        return new JavaProjectHelper(project);
    }
}
