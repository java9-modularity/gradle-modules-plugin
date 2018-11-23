package org.javamodularity.moduleplugin.tasks;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.PackageDeclaration;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;
import org.javamodularity.moduleplugin.TestEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.io.File.pathSeparator;

public class TestTask {

    private static final Logger LOGGER = Logging.getLogger(TestTask.class);

    public void configureTestJava(Project project, String moduleName) {
        Test testJava = (Test) project.getTasks().findByName(JavaPlugin.TEST_TASK_NAME);
        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        SourceSet testSourceSet = javaConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);
        SourceSet mainSourceSet = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        testJava.getExtensions().create("moduleOptions", TestModuleOptions.class, project);

        testJava.doFirst(task -> {

            TestModuleOptions testModuleOptions = testJava.getExtensions().getByType(TestModuleOptions.class);
            if (testModuleOptions.isRunOnClasspath()) {
                LOGGER.lifecycle("Running tests on classpath");
                return;
            }

            var args = new ArrayList<>(testJava.getJvmArgs());

            String testClassesDirs = testSourceSet.getOutput().getClassesDirs().getFiles()
                    .stream().map(File::getPath).collect(Collectors.joining(pathSeparator));

            args.addAll(List.of(
                    "--module-path", testJava.getClasspath().getAsPath(),
                    "--patch-module", moduleName + "=" + testClassesDirs
                            + pathSeparator + mainSourceSet.getOutput().getResourcesDir().toPath()
                            + pathSeparator + testSourceSet.getOutput().getResourcesDir().toPath(),
                    "--add-modules", "ALL-MODULE-PATH"
            ));

            if(!testModuleOptions.getAddModules().isEmpty()) {
                String addModules = String.join(",", testModuleOptions.getAddModules());
                args.add("--add-modules");
                args.add(addModules);
            }

            TestEngine.select(project).ifPresent(testEngine -> {
                args.addAll(List.of("--add-reads", moduleName + "=" + testEngine.moduleName));

                Set<String> testPackages = new HashSet<>();
                for (var sourceFile : testSourceSet.getJava()) {
                    try {
                        Optional<PackageDeclaration> optionalPackageDeclaration = JavaParser.parse(sourceFile).getPackageDeclaration();
                        if (optionalPackageDeclaration.isPresent()) {
                            PackageDeclaration packageDeclaration = optionalPackageDeclaration.get();
                            testPackages.add(packageDeclaration.getNameAsString());
                        }
                    } catch (FileNotFoundException e) {
                        throw new UncheckedIOException(e);
                    }
                }

                testPackages.forEach(p -> {
                    args.add("--add-opens");
                    args.add(String.format("%s/%s=%s", moduleName, p, testEngine.addOpens));
                });
            });

            ModuleInfoTestHelper.mutateArgs(project, moduleName, args::add);

            testJava.setJvmArgs(args);
            testJava.setClasspath(project.files());
        });
    }

}

