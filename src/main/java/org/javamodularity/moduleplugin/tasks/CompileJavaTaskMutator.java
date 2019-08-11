package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.JavaProjectHelper;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;

import java.util.ArrayList;
import java.util.List;

class CompileJavaTaskMutator {

    private static final String COMPILE_KOTLIN_TASK_NAME = "compileKotlin";
    static final String COMPILE_GROOVY_TASK_NAME = "compileGroovy";

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
        List<String> compilerArgs = buildCompilerArgs(javaCompile);
        javaCompile.getOptions().setCompilerArgs(compilerArgs);
        javaCompile.setClasspath(project.files());

        // https://github.com/java9-modularity/gradle-modules-plugin/issues/45
        helper().findTask(COMPILE_KOTLIN_TASK_NAME, AbstractCompile.class)
                .ifPresent(compileKotlin -> javaCompile.setDestinationDir(compileKotlin.getDestinationDir()));
    }

    private List<String> buildCompilerArgs(JavaCompile javaCompile) {
        var compilerArgs = new ArrayList<>(javaCompile.getOptions().getCompilerArgs());

        var patchModuleExtension = helper().extension(PatchModuleExtension.class);

        patchModuleExtension.buildModulePathOption(compileJavaClasspath)
                .ifPresent(option -> option.mutateArgs(compilerArgs));
        patchModuleExtension.resolvePatched(compileJavaClasspath).mutateArgs(compilerArgs);

        moduleOptions.mutateArgs(compilerArgs);

        return compilerArgs;
    }

    private JavaProjectHelper helper() {
        return new JavaProjectHelper(project);
    }
}
