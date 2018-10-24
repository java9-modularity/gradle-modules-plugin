package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.ArrayList;
import java.util.List;

class CompileJavaTaskMutator {

    static void mutateJavaCompileTask(Project project, JavaCompile compileJava) {
        var compilerArgs = new ArrayList<>(compileJava.getOptions().getCompilerArgs());
        compilerArgs.addAll(List.of("--module-path", compileJava.getClasspath().getAsPath()));

        ModuleInfoTestHelper.mutateArgs(project, project.getName(), compilerArgs::add);

        compileJava.getOptions().setCompilerArgs(compilerArgs);
        compileJava.setClasspath(project.files());
    }

}
