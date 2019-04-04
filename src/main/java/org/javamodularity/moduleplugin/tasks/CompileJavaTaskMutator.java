package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;

import java.util.ArrayList;
import java.util.List;

class CompileJavaTaskMutator {

    private static final String COMPILE_KOTLIN_TASK_NAME = "compileKotlin";

    private final Project project;
    /**
     * {@linkplain JavaCompile#getClasspath() Classpath} of {@code compileJava} task.
     */
    private final FileCollection compileJavaClasspath;
    /**
     * {@link CompileModuleOptions} of {@code compileJava} task.
     */
    private final CompileModuleOptions moduleOptions;

    CompileJavaTaskMutator(Project project, FileCollection compileJavaClasspath, CompileModuleOptions moduleOptions) {
        this.project = project;
        this.compileJavaClasspath = compileJavaClasspath;
        this.moduleOptions = moduleOptions;
    }

    /**
     * The argument is a {@link JavaCompile} task whose modularity is to be configured.
     *
     * @param javaCompile {@code compileJava} if {@link CompileModuleOptions#getCompileModuleInfoSeparately()}
     *                    is {@code false}, {@code compileModuleInfoJava} if it is {@code true}
     */
    void modularizeJavaCompileTask(JavaCompile javaCompile) {
        PatchModuleExtension patchModuleExtension = project.getExtensions().getByType(PatchModuleExtension.class);

        var compilerArgs = new ArrayList<>(javaCompile.getOptions().getCompilerArgs());

        compilerArgs.addAll(List.of("--module-path", compileJavaClasspath
                .filter(patchModuleExtension::isUnpatched)
                .getAsPath()));

        String moduleName = (String) project.getExtensions().findByName("moduleName");
        moduleOptions.mutateArgs(moduleName, compilerArgs);

        compilerArgs.addAll(patchModuleExtension.configure(compileJavaClasspath));
        javaCompile.getOptions().setCompilerArgs(compilerArgs);
        javaCompile.setClasspath(project.files());

        // https://github.com/java9-modularity/gradle-modules-plugin/issues/45
        AbstractCompile compileKotlin = (AbstractCompile) project.getTasks().findByName(COMPILE_KOTLIN_TASK_NAME);
        if (compileKotlin != null) {
            javaCompile.setDestinationDir(compileKotlin.getDestinationDir());
        }
    }

}
