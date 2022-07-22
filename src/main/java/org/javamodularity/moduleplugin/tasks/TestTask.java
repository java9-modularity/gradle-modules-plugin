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
import org.gradle.util.GradleVersion;
import org.javamodularity.moduleplugin.TestEngine;
import org.javamodularity.moduleplugin.extensions.TestModuleOptions;
import org.javamodularity.moduleplugin.extensions.PatchModuleContainer;
import org.javamodularity.moduleplugin.internal.TaskOption;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @SuppressWarnings("Convert2Lambda")
    private void configureTestJava(Test testJava) {
        var testModuleOptions = testJava.getExtensions().create("moduleOptions", TestModuleOptions.class, project);

        if(GradleVersion.current().compareTo(GradleVersion.version("6.4")) >= 0) {
            testJava.getModularity().getInferModulePath().set(false);
        }
        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        testJava.doFirst(new Action<>() {
            @Override
            public void execute(Task task) {
                if (testModuleOptions.getRunOnClasspath()) {
                    LOGGER.lifecycle("Running tests on classpath");
                    return;
                }

                List<String> args = buildJvmArgs(testJava, testModuleOptions);
                testJava.setJvmArgs(args);
                LOGGER.info("jvmArgs for task {}: {}", task.getName(), args);
                testJava.setClasspath(project.files());
            }
        });
    }

    private List<String> buildJvmArgs(Test testJava, TestModuleOptions testModuleOptions) {
        var jvmArgs = new ArrayList<>(testJava.getJvmArgs());


        FileCollection classpath = mergeClassesHelper().getMergeAdjustedClasspath(testJava.getClasspath());
        var patchModuleContainer = PatchModuleContainer.copyOf(
                helper().modularityExtension().optionContainer().getPatchModuleContainer());
        String moduleName = helper().moduleName();
        buildPatchModulePathStream().forEach(path -> patchModuleContainer.addDir(moduleName, path.toString()));
        patchModuleContainer.buildModulePathOption(classpath).ifPresent(option -> option.mutateArgs(jvmArgs));

        patchModuleContainer.mutator(classpath).mutateArgs(jvmArgs);

        new TaskOption("--add-modules", "ALL-MODULE-PATH").mutateArgs(jvmArgs);

        testModuleOptions.mutateArgs(jvmArgs);

        TestEngine.selectMultiple(project, classpath.getFiles()).forEach(testEngine -> {
            buildAddReadsOption(testEngine).mutateArgs(jvmArgs);
            buildAddOpensOptionStream(testEngine).forEach(option -> option.mutateArgs(jvmArgs));
            testEngine.additionalTaskOptions.forEach(option -> option.mutateArgs(jvmArgs));
        });

        ModuleInfoTestHelper.mutateArgs(project, false, jvmArgs::add);

        return jvmArgs;
    }

    private Stream<Path> buildPatchModulePathStream() {
        SourceSet mainSourceSet = helper().mainSourceSet();
        SourceSet testSourceSet = helper().testSourceSet();
        Optional<SourceSet> testFixturesSourceSet = helper().findTestFixturesSourceSet();

        var classesSourceSets = new ArrayList<SourceSet>();
        classesSourceSets.add(testSourceSet);
        testFixturesSourceSet.ifPresent(classesSourceSets::add);

        var sourceSets = new ArrayList<SourceSet>();
        sourceSets.add(mainSourceSet);
        sourceSets.addAll(classesSourceSets);

        Stream<Path> classesFileStream = classesSourceSets.stream()
                .flatMap(sourceSet -> sourceSet.getOutput().getClassesDirs().getFiles().stream())
                .map(File::toPath);

        Stream<Path> resourceFileStream = sourceSets.stream()
                .map(sourceSet -> sourceSet.getOutput().getResourcesDir())
                .filter(Objects::nonNull)
                .map(File::toPath)
                .filter(Files::isDirectory);

        return Stream.concat(classesFileStream, resourceFileStream);
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
