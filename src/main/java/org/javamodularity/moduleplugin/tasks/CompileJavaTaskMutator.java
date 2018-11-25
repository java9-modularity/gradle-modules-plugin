package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.ArrayList;
import java.util.List;

class CompileJavaTaskMutator {

    static void mutateJavaCompileTask(Project project, JavaCompile compileJava) {
        ModuleOptions moduleOptions = compileJava.getExtensions().getByType(ModuleOptions.class);

        var compilerArgs = new ArrayList<>(compileJava.getOptions().getCompilerArgs());
        compilerArgs.addAll(List.of("--module-path", compileJava.getClasspath().getAsPath()));

        if(!moduleOptions.getAddModules().isEmpty()) {
            String addModules = String.join(",", moduleOptions.getAddModules());
            compilerArgs.add("--add-modules");
            compilerArgs.add(addModules);
        }

        ModuleInfoTestHelper.mutateArgs(project, project.getName(), compilerArgs::add);
        compileJava.getOptions().setCompilerArgs(compilerArgs);
        compileJava.setClasspath(project.files());

        AbstractCompile compileKotlin = (AbstractCompile)project.getTasks().findByName("compileKotlin");
        if (compileKotlin != null) {
            compileJava.setDestinationDir(compileKotlin.getDestinationDir());
        }
    }

}
