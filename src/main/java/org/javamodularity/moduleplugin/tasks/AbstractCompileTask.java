package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.JavaProjectHelper;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;

abstract class AbstractCompileTask {

    protected final Project project;

    AbstractCompileTask(Project project) {
        this.project = project;
    }

    final CompileJavaTaskMutator createCompileJavaTaskMutator(
            JavaCompile compileJava, CompileModuleOptions moduleOptions) {
        return new CompileJavaTaskMutator(project, compileJava.getClasspath(), moduleOptions);
    }

    final JavaProjectHelper helper() {
        return new JavaProjectHelper(project);
    }
}
