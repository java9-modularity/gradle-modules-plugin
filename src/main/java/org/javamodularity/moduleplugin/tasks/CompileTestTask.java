package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.JavaProjectHelper;
import org.javamodularity.moduleplugin.TestEngine;
import org.javamodularity.moduleplugin.extensions.CompileTestModuleOptions;
import org.javamodularity.moduleplugin.extensions.PatchModuleContainer;
import org.javamodularity.moduleplugin.internal.TaskOption;

import java.util.ArrayList;
import java.util.List;

public class CompileTestTask extends AbstractModulePluginTask {
    private static final Logger LOGGER = Logging.getLogger(CompileTestTask.class);

    public CompileTestTask(Project project) {
        super(project);
    }

    public void configureCompileTestJava() {
        helper().findTask(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME, JavaCompile.class)
                .ifPresent(this::configureCompileTestJava);
        project.afterEvaluate(p -> {
            helper().findTask(JavaProjectHelper.COMPILE_TEST_FIXTURES_JAVA_TASK_NAME, JavaCompile.class)
                    .ifPresent(task -> task.exclude("module-info.java"));
        });
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
                LOGGER.info("compiler args for task {}: {}", compileTestJava.getName(),
                        compileTestJava.getOptions().getAllCompilerArgs());
                compileTestJava.setClasspath(project.files());
            }
        });
    }

    private List<String> buildCompilerArgs(
            JavaCompile compileTestJava, CompileTestModuleOptions moduleOptions) {
        var compilerArgs = new ArrayList<>(compileTestJava.getOptions().getCompilerArgs());

        String moduleName = helper().moduleName();
        FileCollection classpath = mergeClassesHelper().getMergeAdjustedClasspath(compileTestJava.getClasspath());

        var patchModuleContainer = PatchModuleContainer.copyOf(
                helper().modularityExtension().optionContainer().getPatchModuleContainer());
        FileCollection testSourceDirs = helper().testSourceSet().getJava().getSourceDirectories();
        FileCollection testFixturesSourceDirs = helper().findTestFixturesSourceSet()
                .map(sourceSet -> sourceSet.getJava().getSourceDirectories())
                .orElse(project.files());
        FileCollection allTestSourceDirs = testSourceDirs.plus(testFixturesSourceDirs);

        allTestSourceDirs.forEach(dir -> patchModuleContainer.addDir(moduleName, dir.getAbsolutePath()));
        patchModuleContainer.buildModulePathOption(classpath).ifPresent(option -> option.mutateArgs(compilerArgs));

        TestEngine.selectMultiple(project, classpath.getFiles()).forEach(testEngine -> {
            new TaskOption("--add-modules", testEngine.moduleName).mutateArgs(compilerArgs);
            new TaskOption("--add-reads", moduleName + "=" + testEngine.moduleName).mutateArgs(compilerArgs);
            testEngine.additionalTaskOptions.forEach(option -> option.mutateArgs(compilerArgs));
        });

        moduleOptions.mutateArgs(compilerArgs);

        patchModuleContainer.mutator(classpath).mutateArgs(compilerArgs);

        ModuleInfoTestHelper.mutateArgs(project, compilerArgs::add);

        return compilerArgs;
    }

}
