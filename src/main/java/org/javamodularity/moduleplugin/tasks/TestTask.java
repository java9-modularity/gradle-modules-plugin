package org.javamodularity.moduleplugin.tasks;

import static java.io.File.pathSeparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codehaus.groovy.tools.Utilities;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;
import org.javamodularity.moduleplugin.TestEngine;

public class TestTask {
    private static final Logger LOGGER = Logging.getLogger(TestTask.class);
    private static Pattern CLASS_FILE_SPLITTER = Pattern.compile("[./\\\\]");

    public void configureTestJava(Project project, String moduleName) {
        Test testJava = (Test) project.getTasks().findByName(JavaPlugin.TEST_TASK_NAME);
        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        PatchModuleExtension patchModuleExtension = project.getExtensions().getByType(PatchModuleExtension.class);

        SourceSet testSourceSet = javaConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);
        SourceSet mainSourceSet = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        testJava.getExtensions().create("moduleOptions", TestModuleOptions.class, project);

        testJava.doFirst(new Action<Task>() {

            /* (non-Javadoc)
             * @see org.gradle.api.Action#execute(java.lang.Object)
             */
            @Override
            public void execute(Task task) {
                TestModuleOptions testModuleOptions = testJava.getExtensions().getByType(TestModuleOptions.class);
                if (testModuleOptions.isRunOnClasspath()) {
                    LOGGER.lifecycle("Running tests on classpath");
                    return;
                }

                var args = new ArrayList<>(testJava.getJvmArgs());

                String testClassesDirs = testSourceSet.getOutput().getClassesDirs().getFiles()
                        .stream().map(File::getPath).collect(Collectors.joining(pathSeparator));

                args.addAll(List.of(
                        "--module-path", testJava.getClasspath()
                                .filter(patchModuleExtension::isUnpatched)
                                .getAsPath(),
                        "--patch-module", moduleName + "=" + testClassesDirs
                                + pathSeparator + mainSourceSet.getOutput().getResourcesDir().toPath()
                                + pathSeparator + testSourceSet.getOutput().getResourcesDir().toPath(),
                        "--add-modules", "ALL-MODULE-PATH"
                ));

                if (!testModuleOptions.getAddModules().isEmpty()) {
                    String addModules = String.join(",", testModuleOptions.getAddModules());
                    args.add("--add-modules");
                    args.add(addModules);
                }

                args.addAll(patchModuleExtension.configure(testJava.getClasspath()));

                TestEngine.select(project).ifPresent(new Consumer<TestEngine>() {

                    /* (non-Javadoc)
                     * @see java.util.function.Consumer#accept(java.lang.Object)
                     */
                    @Override
                    public void accept(TestEngine testEngine) {
                        args.addAll(List.of("--add-reads", moduleName + "=" + testEngine.moduleName));

                        Set<File> testDirs = testSourceSet.getOutput().getClassesDirs().getFiles();
                        getPackages(testDirs).forEach(new Consumer<String>() {

                            /* (non-Javadoc)
                             * @see java.util.function.Consumer#accept(java.lang.Object)
                             */
                            @Override
                            public void accept(String p) {
                                args.add("--add-opens");
                                args.add(String.format("%s/%s=%s", moduleName, p, testEngine.addOpens));
                            }

                        });
                    }

                });

                ModuleInfoTestHelper.mutateArgs(project, moduleName, args::add);

                testJava.setJvmArgs(args);
                testJava.setClasspath(project.files());
            }

        });
    }

    private static Set<String> getPackages(Collection<File> dirs) {
        Set<String> packages = new TreeSet<>();
        for (File dir : dirs) {
            LOGGER.debug("Scanning packages in " + dir);
            if (dir.isDirectory()) {
                Path dirPath = dir.toPath();
                try (Stream<Path> entries = Files.walk(dirPath)) {
                    entries.forEach(entry -> {
                        if (entry.toFile().isFile()) {
                            String path = entry.toString();
                            if (isValidClassFileReference(path)) {
                                Path relPath = dirPath.relativize(entry.getParent());
                                packages.add(relPath.toString().replace(File.separatorChar, '.'));
                            }
                        }
                    });
                } catch (IOException e) {
                    throw new GradleException("Failed to scan " + dir, e);
                }
            }
        }
        LOGGER.debug("Found packages: " + packages);
        return packages;
    }

    private static boolean isValidClassFileReference(String path) {
        if (!path.endsWith(".class")) return false;
        String name = path.substring(0, path.length() - ".class".length());
        String[] tokens = CLASS_FILE_SPLITTER.split(name);
        if (tokens.length == 0) return false;
        return Utilities.isJavaIdentifier(tokens[tokens.length - 1]);
    }
}
