package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.TestEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CompileTestTask {
    public void configureCompileTestJava(Project project, String moduleName) {
        final JavaCompile compileTestJava = (JavaCompile) project.getTasks().findByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME);
        JavaPluginConvention javaConvention =
                project.getConvention().getPlugin(JavaPluginConvention.class);

        SourceSet testSourceSet = javaConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

        compileTestJava.doFirst(task -> {
            var args = new ArrayList<>(compileTestJava.getOptions().getCompilerArgs());
            args.addAll(List.of(
                    "--module-path", compileTestJava.getClasspath().getAsPath(),
                    "--patch-module", moduleName + "=" + testSourceSet.getJava().getSourceDirectories().getAsPath()
            ));

            var configurations = project.getConfigurations();
            var testImplementation = configurations.getByName("testImplementation").getDependencies().stream();
            var testCompile = configurations.getByName("testCompile").getDependencies().stream();
            Optional<TestEngine> testEngine = Stream.concat(testImplementation, testCompile)
                    .map(d -> TestEngine.select(d.getGroup(), d.getName()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findAny();

            if (testEngine.isPresent()) {
                String testEngineModuleName = testEngine.get().getModuleName();
                args.addAll(List.of(
                        "--add-modules", testEngineModuleName,
                        "--add-reads", moduleName + "=" + testEngineModuleName));
            }

            compileTestJava.getOptions().setCompilerArgs(args);
            compileTestJava.setClasspath(project.files());
        });
    }
}
