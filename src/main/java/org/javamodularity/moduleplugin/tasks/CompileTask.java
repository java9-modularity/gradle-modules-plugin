package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.Optional;

public class CompileTask {
    public void configureCompileJava(Project project) {
        final JavaCompile compileJava = (JavaCompile) project.getTasks().findByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);
        Optional.ofNullable(compileJava)
                .ifPresent(cj ->
                        cj.doFirst(task -> CompileJavaTaskMutator.mutateJavaCompileTask(project, compileJava)));

    }

}
