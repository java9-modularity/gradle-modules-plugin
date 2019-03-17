package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.JavaProjectHelper;

public class CompileTask {

    private final Project project;

    public CompileTask(Project project) {
        this.project = project;
    }

    public void configureCompileJava() {
        helper().findCompileJavaTask(JavaPlugin.COMPILE_JAVA_TASK_NAME)
                .ifPresent(this::configureCompileJava);
    }

    private void configureCompileJava(JavaCompile compileJava) {
        compileJava.getExtensions().create("moduleOptions", ModuleOptions.class, project);

        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        compileJava.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                CompileJavaTaskMutator.mutateJavaCompileTask(project, compileJava);
            }
        });
    }

    private JavaProjectHelper helper() {
        return new JavaProjectHelper(project);
    }
}
