package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.ArrayList;
import java.util.List;

class CompileJavaTaskMutator {

    static void mutateJavaCompileTask(Project project, JavaCompile compileJava) {
        ModuleOptions moduleOptions = compileJava.getExtensions().getByType(ModuleOptions.class);
        PatchModuleExtension patchModuleExtension = project.getExtensions().getByType(PatchModuleExtension.class);

        var compilerArgs = new ArrayList<>(compileJava.getOptions().getCompilerArgs());

        compilerArgs.addAll(List.of("--module-path", compileJava.getClasspath()
                .filter(patchModuleExtension::isUnpatched)
                .getAsPath()));

        String moduleName = (String) project.getExtensions().getByName("moduleName");
        moduleOptions.mutateArgs(moduleName, compilerArgs);

        compilerArgs.addAll(patchModuleExtension.configure(compileJava.getClasspath()));
        compileJava.getOptions().setCompilerArgs(compilerArgs);
        compileJava.setClasspath(project.files());

        AbstractCompile compileKotlin = (AbstractCompile) project.getTasks().findByName("compileKotlin");
        if (compileKotlin != null) {
            compileJava.setDestinationDir(compileKotlin.getDestinationDir());
        }
    }

}
