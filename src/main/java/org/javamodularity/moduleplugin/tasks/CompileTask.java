package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.ArrayList;
import java.util.List;

public class CompileTask {
    public void configureCompileJava(Project project) {
        final JavaCompile compileJava = (JavaCompile) project.getTasks().findByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);
        compileJava.doFirst(task -> {
            var compilerArgs = new ArrayList<>(compileJava.getOptions().getCompilerArgs());
            compilerArgs.addAll(List.of("--module-path", compileJava.getClasspath().getAsPath()));

            compileJava.getOptions().setCompilerArgs(compilerArgs);
            compileJava.setClasspath(project.files());
        });
    }
}
