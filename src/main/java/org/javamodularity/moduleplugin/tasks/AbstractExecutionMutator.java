package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.JavaExec;
import org.gradle.util.GradleVersion;
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
        if(GradleVersion.current().compareTo(GradleVersion.version("6.4")) < 0) {
            String mainClassName = Objects.requireNonNull(
                    execTask.getMain(),
                    "Main class name not found. Try setting 'application.mainClassName' in your Gradle build file."
            );
            if (!mainClassName.contains("/")) {
                    LOGGER.warn("No module was provided for main class, assuming the current module. Prefer providing 'mainClassName' in the following format: '$moduleName/a.b.Main'");
                return helper().moduleName() + "/" + mainClassName;
            }
            return mainClassName;
        } else {
            String mainClassName = Objects.requireNonNull(
                    execTask.getMainClass().getOrNull(),
                    "Main class name not found. Try setting 'application.mainClass' in your Gradle build file."
            );
            String mainModuleName = execTask.getMainModule().getOrNull();
            if(mainModuleName == null) {
                LOGGER.warn("Main module name not found. Try setting 'application.mainModule' in your Gradle build file.");
                mainModuleName = helper().moduleName();
            }
            return mainModuleName + "/" + mainClassName;
        }
    }

    protected final JavaProjectHelper helper() {
        return new JavaProjectHelper(project);
    }

    protected final MergeClassesHelper mergeClassesHelper() {
        return new MergeClassesHelper(project);
    }
}
