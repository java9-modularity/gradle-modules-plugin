package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;

abstract class AbstractCompileTask extends AbstractModulePluginTask {

    AbstractCompileTask(Project project) {
        super(project);
    }

    final CompileJavaTaskMutator createCompileJavaTaskMutator(
            JavaCompile compileJava, CompileModuleOptions moduleOptions) {
        return new CompileJavaTaskMutator(project, compileJava.getClasspath(), moduleOptions);
    }
}
