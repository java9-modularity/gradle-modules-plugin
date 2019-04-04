package org.javamodularity.moduleplugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.Optional;

/**
 * Generic helper for Gradle {@link Project} API that has {@link JavaPlugin} applied.
 */
public final class JavaProjectHelper {

    private final Project project;

    public JavaProjectHelper(Project project) {
        this.project = project;
    }

    public Project project() {
        return project;
    }

    //region SOURCE SETS
    public SourceSetContainer sourceSets() {
        return project.getExtensions().getByType(SourceSetContainer.class);
    }

    public SourceSet sourceSet(String sourceSetName) {
        return sourceSets().getByName(sourceSetName);
    }

    public SourceSet mainSourceSet() {
        return sourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
    }

    public SourceSet testSourceSet(String sourceSetName) {
        return sourceSet(SourceSet.TEST_SOURCE_SET_NAME);
    }
    //endregion

    //region TASKS
    public Task task(String taskName) {
        return project.getTasks().getByName(taskName);
    }

    public JavaCompile compileJavaTask(String taskName) {
        return (JavaCompile) task(taskName);
    }

    public Optional<Task> findTask(String taskName) {
        return Optional.ofNullable(project.getTasks().findByName(taskName));
    }

    public Optional<JavaCompile> findCompileJavaTask(String taskName) {
        return findTask(taskName).map(JavaCompile.class::cast);
    }
    //endregion

}
