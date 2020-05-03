package org.javamodularity.moduleplugin.tasks;

import org.codehaus.groovy.tools.Utilities;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;
import org.javamodularity.moduleplugin.TestEngine;
import org.javamodularity.moduleplugin.extensions.TestModuleOptions;
import org.javamodularity.moduleplugin.internal.PatchModuleContainer;
import org.javamodularity.moduleplugin.internal.TaskOption;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.io.File.pathSeparator;

public class TestTask extends AbstractModulePluginTask {

    private static final Logger LOGGER = Logging.getLogger(TestTask.class);
    private static final Pattern CLASS_FILE_SPLITTER = Pattern.compile("[./\\\\]");

    public TestTask(Project project) {
        super(project);
    }

    public void configureTestJava() {
        helper().findTask(JavaPlugin.TEST_TASK_NAME, Test.class)
                .ifPresent(this::configureTestJava);
    }

    private void configureTestJava(Test testJava) {
        var testModuleOptions = testJava.getExtensions().create("moduleOptions", TestModuleOptions.class, project);

        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        testJava.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                if (testModuleOptions.getRunOnClasspath()) {
                    LOGGER.lifecycle("Running tests on classpath");
                    return;
                }

                List<String> args = buildJvmArgs(testJava, testModuleOptions);
                testJava.setJvmArgs(args);
                testJava.setClasspath(project.files());
            }
        });
    }

    private List<String> buildJvmArgs(Test testJava, TestModuleOptions testModuleOptions) {
        var jvmArgs = new ArrayList<>(testJava.getJvmArgs());


        FileCollection classpath = mergeClassesHelper().getMergeAdjustedClasspath(testJava.getClasspath());
        var patchModuleContainer = PatchModuleContainer.copyOf(helper().modularityExtension().patchModuleContainer());
        String moduleName = helper().moduleName();
        buildPatchModulePathStream().forEach(path -> patchModuleContainer.addDir(moduleName, path.toString()));
        patchModuleContainer.buildModulePathOption(classpath).ifPresent(option -> option.mutateArgs(jvmArgs));

        patchModuleContainer.mutator(classpath).mutateArgs(jvmArgs);

        new TaskOption("--add-modules", "ALL-MODULE-PATH").mutateArgs(jvmArgs);

        testModuleOptions.mutateArgs(jvmArgs);

        TestEngine.selectMultiple(project).forEach(testEngine -> {
            buildAddReadsOption(testEngine).mutateArgs(jvmArgs);
            buildAddOpensOptionStream(testEngine).forEach(option -> option.mutateArgs(jvmArgs));
        });

        ModuleInfoTestHelper.mutateArgs(project, jvmArgs::add);

        return jvmArgs;
    }

    private Stream<Path> buildPatchModulePathStream() {
        SourceSet testSourceSet = helper().testSourceSet();
        SourceSet mainSourceSet = helper().mainSourceSet();

        Stream<File> classesFileStream = testSourceSet.getOutput().getClassesDirs().getFiles().stream();
        Stream<File> resourceFileStream = Stream.of(mainSourceSet, testSourceSet)
                .map(sourceSet -> sourceSet.getOutput().getResourcesDir());

        return Stream.concat(classesFileStream, resourceFileStream).map(File::toPath);
    }

    private TaskOption buildAddReadsOption(TestEngine testEngine) {
        String moduleName = helper().moduleName();
        return new TaskOption("--add-reads", moduleName + "=" + testEngine.moduleName);
    }

    private Stream<TaskOption> buildAddOpensOptionStream(TestEngine testEngine) {
        String moduleName = helper().moduleName();
        Set<File> testDirs = helper().testSourceSet().getOutput().getClassesDirs().getFiles();

        return getPackages(testDirs).stream()
                .map(packageName -> String.format("%s/%s=%s", moduleName, packageName, testEngine.addOpens))
                .map(value -> new TaskOption("--add-opens", value));
    }

    private static Set<String> getPackages(Collection<File> dirs) {
        Set<String> packages = dirs.stream()
                .peek(dir -> LOGGER.debug("Scanning packages in " + dir))
                .map(File::toPath)
                .filter(Files::isDirectory)
                .flatMap(TestTask::buildRelativePathStream)
                .map(relPath -> relPath.toString().replace(File.separatorChar, '.'))
                .collect(Collectors.toCollection(TreeSet::new));
        LOGGER.debug("Found packages: " + packages);
        return packages;
    }

    private static Stream<Path> buildRelativePathStream(Path dir) {
        try {
            return Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .filter(path -> isValidClassFileReference(path.toString()))
                    .map(path -> dir.relativize(path.getParent()));
        } catch (IOException e) {
            throw new GradleException("Failed to scan " + dir, e);
        }
    }

    private static boolean isValidClassFileReference(String path) {
        if (!path.endsWith(".class")) return false;
        String name = path.substring(0, path.length() - ".class".length());
        String[] tokens = CLASS_FILE_SPLITTER.split(name);
        if (tokens.length == 0) return false;
        return Utilities.isJavaIdentifier(tokens[tokens.length - 1]);
    }
}
