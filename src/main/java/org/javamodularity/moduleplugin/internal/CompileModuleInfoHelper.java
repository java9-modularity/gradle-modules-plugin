package org.javamodularity.moduleplugin.internal;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.util.GradleVersion;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public final class CompileModuleInfoHelper {

    private static final Logger LOGGER = Logging.getLogger(CompileModuleInfoHelper.class);

    /**
     * If this project depends on projects that are using separate {@code module-info.java} compilation,
     * {@code javaCompile} has to depend on {@code compileModuleInfoJava} of every such dependent project
     * (otherwise, {@code module-info.java} of this project may not compile).
     *
     * @param javaCompile compile Java task to be modularized
     */
    public static void dependOnOtherCompileModuleInfoJavaTasks(JavaCompile javaCompile) {
        dependentCompileModuleInfoJavaTaskStream(javaCompile.getProject())
                .peek(compileModuleInfoJava -> LOGGER.debug("{}.dependsOn({})", javaCompile, compileModuleInfoJava))
                .forEach(javaCompile::dependsOn);
    }

    /**
     * @return a {@link Stream} of {@code compileModuleInfoJava} tasks from dependent projects
     */
    private static Stream<Task> dependentCompileModuleInfoJavaTaskStream(Project project) {
        final Function<Dependency, TaskContainer> mapToTaskContainer = dependency ->
                project.project(((ProjectDependency) dependency)
                        .getPath() /* ProjectDependency.getPath() introduced in Gradle 8.11 */).getTasks();

        return project.getConfigurations().stream()
                .flatMap(configuration -> configuration.getDependencies().stream())
                .filter(dependency -> dependency instanceof ProjectDependency)
                .map(mapToTaskContainer)
                .map(tasks -> tasks.findByName(CompileModuleOptions.COMPILE_MODULE_INFO_TASK_NAME))
                .filter(Objects::nonNull)
                .filter(task -> task.getProject() != project);
    }
}
