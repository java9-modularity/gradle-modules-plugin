package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.JavaProjectHelper;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class CompileJavaTaskMutator {

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
        configureSourcepath(javaCompile);
    }

    // Setting the sourcepath is necessary when using forked compilation for module-info.java
    private void configureSourcepath(JavaCompile javaCompile) {
        helper().mainSourceSet().getJava().getSrcDirs().stream()
                .map(srcDir -> srcDir.toPath().resolve("module-info.java"))
                .filter(Files::exists)
                .findFirst()
                .ifPresent(path -> javaCompile.getOptions().setSourcepath(project.files(path.getParent())));
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
