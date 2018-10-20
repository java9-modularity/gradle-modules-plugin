package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.TestEngine;

import java.util.ArrayList;
import java.util.List;

public class CompileTestTask {

    public void configureCompileTestJava(Project project, String moduleName) {
        JavaCompile compileTestJava = (JavaCompile) project.getTasks().findByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME);
        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        SourceSet testSourceSet = javaConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

        compileTestJava.doFirst(task -> {
            var args = new ArrayList<>(compileTestJava.getOptions().getCompilerArgs());
            args.addAll(List.of(
                    "--module-path", compileTestJava.getClasspath().getAsPath(),
                    "--patch-module", moduleName + "=" + testSourceSet.getJava().getSourceDirectories().getAsPath()
            ));

            TestEngine.select(project).ifPresent(testEngine -> {
                args.addAll(List.of(
                        "--add-modules", testEngine.moduleName,
                        "--add-reads", moduleName + "=" + testEngine.moduleName));
            });

            compileTestJava.getOptions().setCompilerArgs(args);
            compileTestJava.setClasspath(project.files());
        });
    }

}
