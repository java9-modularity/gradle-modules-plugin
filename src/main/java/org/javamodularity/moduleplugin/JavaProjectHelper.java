package org.javamodularity.moduleplugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.Optional;

/**
 * Generic helper for Gradle {@link Project} API that is modular and has {@link JavaPlugin} applied.
 */
public final class JavaProjectHelper {

    private final Project project;

    public JavaProjectHelper(Project project) {
        this.project = project;
    }

    public Project project() {
        return project;
    }

    public <T> T extension(Class<T> extensionType) {
        return project.getExtensions().getByType(extensionType);
    }

    public <T> T extension(String name, Class<T> extensionType) {
        return extensionType.cast(project.getExtensions().getByName(name));
    }

    public String moduleName() {
        return extension("moduleName", String.class);
    }

    //region SOURCE SETS
    public SourceSetContainer sourceSets() {
        return extension(SourceSetContainer.class);
    }

    public SourceSet sourceSet(String sourceSetName) {
        return sourceSets().getByName(sourceSetName);
    }

    public SourceSet mainSourceSet() {
        return sourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
    }

    public SourceSet testSourceSet() {
        return sourceSet(SourceSet.TEST_SOURCE_SET_NAME);
    }
    //endregion

    //region TASKS
    public Task task(String taskName) {
        return project.getTasks().getByName(taskName);
    }

    public Optional<Task> findTask(String taskName) {
        return Optional.ofNullable(project.getTasks().findByName(taskName));
    }

    public <T extends Task> T task(String taskName, Class<T> taskType) {
        return taskType.cast(task(taskName));
    }

    public <T extends Task> Optional<T> findTask(String taskName, Class<T> taskType) {
        return findTask(taskName).map(taskType::cast);
    }

    public JavaCompile compileJavaTask(String taskName) {
        return task(taskName, JavaCompile.class);
    }
    //endregion

    //region DIRECTORIES
    public File getMergedDir() {
        return new File(project.getBuildDir(), "classes/merged");
    }

    public File getModuleInfoDir() {
        return new File(project.getBuildDir(), "classes/module-info");
    }
    //endregion
}
