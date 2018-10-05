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
import java.util.*;

public class TestTask {
    private static final Logger LOGGER = Logging.getLogger(TestTask.class);

    public void configureTestJava(Project project, String moduleName) {
        final Test testJava = (Test) project.getTasks().findByName(JavaPlugin.TEST_TASK_NAME);
        JavaPluginConvention javaConvention =
                project.getConvention().getPlugin(JavaPluginConvention.class);


        SourceSet testSourceSet = javaConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);
        testJava.getExtensions().create("moduleOptions", TestModuleOptions.class, project);

        testJava.doFirst(task -> {

            TestModuleOptions testModuleOptions = testJava.getExtensions().getByType(TestModuleOptions.class);
            if(testModuleOptions.isRunOnClasspath()) {
                LOGGER.lifecycle("Running tests on classpath");
                return;
            }

            var args = new ArrayList<>(testJava.getJvmArgs());

            args.addAll(List.of(
                    "--module-path", testJava.getClasspath().getAsPath(),
                    "--patch-module", moduleName + "=" + testSourceSet.getJava().getOutputDir().toPath(),
                    "--add-modules", "ALL-MODULE-PATH"
            ));

            Optional<TestEngine> testEngine = project.getConfigurations().getByName("testCompile").getDependencies().stream()
                    .map(d -> TestEngine.select(d.getGroup(), d.getName()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findAny();

            if (testEngine.isPresent()) {
                String testEngineModuleName = testEngine.get().getModuleName();
                args.addAll(List.of(
                        "--add-reads", moduleName + "=" + testEngineModuleName));

                Iterator<File> iterator = testSourceSet.getJava().iterator();
                Set<String> testPackages = new HashSet<>();

                while (iterator.hasNext()) {

                    try {
                        Optional<PackageDeclaration> optionalPackageDeclaration = JavaParser.parse(iterator.next()).getPackageDeclaration();
                        if (optionalPackageDeclaration.isPresent()) {
                            PackageDeclaration packageDeclaration = optionalPackageDeclaration.get();
                            testPackages.add(packageDeclaration.getNameAsString());
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                testPackages.forEach(p -> {
                    args.add("--add-opens");
                    args.add(String.format("%s/%s=%s", moduleName, p, testEngine.get().getAddOpens()));


                });
            }

            testJava.setJvmArgs(args);
            testJava.setClasspath(project.files());
        });
    }

}

