package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.TestEngine;
import org.javamodularity.moduleplugin.extensions.CompileTestModuleOptions;
import org.javamodularity.moduleplugin.extensions.PatchModuleExtension;
import org.javamodularity.moduleplugin.internal.TaskOption;

import java.util.ArrayList;
import java.util.List;

public class CompileTestTask extends AbstractModulePluginTask {

    public CompileTestTask(Project project) {
        super(project);
    }

    public void configureCompileTestJava() {
        helper().findTask(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME, JavaCompile.class)
                .ifPresent(this::configureCompileTestJava);
    }

    private void configureCompileTestJava(JavaCompile compileTestJava) {
        var moduleOptions = compileTestJava.getExtensions()
                .create("moduleOptions", CompileTestModuleOptions.class, project);

        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        compileTestJava.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                var compilerArgs = buildCompilerArgs(compileTestJava, moduleOptions);
                compileTestJava.getOptions().setCompilerArgs(compilerArgs);
                compileTestJava.setClasspath(project.files());
            }
        });
    }

    private List<String> buildCompilerArgs(
            JavaCompile compileTestJava, CompileTestModuleOptions moduleOptions) {
        var compilerArgs = new ArrayList<>(compileTestJava.getOptions().getCompilerArgs());

        String moduleName = helper().moduleName();
        var patchModuleExtension = helper().extension(PatchModuleExtension.class);
        FileCollection classpath = mergeClassesHelper().getMergeAdjustedClasspath(compileTestJava.getClasspath());

        patchModuleExtension.buildModulePathOption(classpath).ifPresent(option -> option.mutateArgs(compilerArgs));

        new TaskOption(
                "--patch-module",
                moduleName + "=" + helper().testSourceSet().getJava().getSourceDirectories().getAsPath()
        ).mutateArgs(compilerArgs);

        TestEngine.selectMultiple(project).forEach(testEngine -> {
            new TaskOption("--add-modules", testEngine.moduleName).mutateArgs(compilerArgs);
            new TaskOption("--add-reads", moduleName + "=" + testEngine.moduleName).mutateArgs(compilerArgs);
        });

        moduleOptions.mutateArgs(compilerArgs);

        patchModuleExtension.resolvePatched(classpath).mutateArgs(compilerArgs);

        ModuleInfoTestHelper.mutateArgs(project, compilerArgs::add);

        return compilerArgs;
    }

}
