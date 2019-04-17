package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.TestEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CompileTestTask extends AbstractModulePluginTask {

    public CompileTestTask(Project project) {
        super(project);
    }

    public void configureCompileTestJava() {
        helper().findTask(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME, JavaCompile.class)
                .ifPresent(this::configureCompileTestJava);
    }

    private void configureCompileTestJava(JavaCompile compileTestJava) {
        var moduleOptions = compileTestJava.getExtensions().create("moduleOptions", ModuleOptions.class, project);

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

    private List<String> buildCompilerArgs(JavaCompile compileTestJava, ModuleOptions moduleOptions) {
        var compilerArgs = new ArrayList<>(compileTestJava.getOptions().getCompilerArgs());

        String moduleName = helper().moduleName();
        var patchModuleExtension = helper().extension(PatchModuleExtension.class);

        Stream.of(
                "--module-path",
                patchModuleExtension.getUnpatchedClasspathAsPath(compileTestJava.getClasspath()),
                "--patch-module",
                moduleName + "=" + helper().testSourceSet().getJava().getSourceDirectories().getAsPath()
        ).forEach(compilerArgs::add);

        TestEngine.select(project).ifPresent(testEngine -> Stream.of(
                "--add-modules", testEngine.moduleName,
                "--add-reads", moduleName + "=" + testEngine.moduleName
        ).forEach(compilerArgs::add));

        moduleOptions.mutateArgs(moduleName, compilerArgs);

        patchModuleExtension.resolve(compileTestJava.getClasspath()).toArgumentStream()
                .forEach(compilerArgs::add);

        ModuleInfoTestHelper.mutateArgs(project, compilerArgs::add);

        return compilerArgs;
    }

}
