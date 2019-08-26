package org.javamodularity.moduleplugin;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ConstantConditions")
class ModulePluginSmokeTest {

    private static final String GRADLE_VERSION = "5.3.1";

    private List<File> pluginClasspath;

    @BeforeEach
    void before() throws IOException {
        pluginClasspath = Resources.readLines(Resources.getResource("plugin-classpath.txt"), Charsets.UTF_8)
                .stream()
                .map(File::new)
                .collect(Collectors.toList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test-project", "test-project-kotlin", "test-project-groovy"})
    void smokeTest(String projectName) {
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(GRADLE_VERSION)
                .withArguments("-c", "smoke_test_settings.gradle", "clean", "build", "run", "--stacktrace")
                .forwardOutput()
                .build();

        assertTasksSuccessful(result, "greeter.api", "build");
        assertTasksSuccessful(result, "greeter.provider", "build");
        assertTasksSuccessful(result, "greeter.provider.test", "build");
        assertTasksSuccessful(result, "greeter.runner", "build", "run");
    }

    @Test
    void smokeTestMixed() throws IOException {
        var result = GradleRunner.create()
                .withProjectDir(new File("test-project-mixed"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(GRADLE_VERSION)
                .withArguments("-c", "smoke_test_settings.gradle", "clean", "build", "--stacktrace")
                .forwardOutput()
                .build();

        verifyMixedTestResult(result, "greeter.api-jdk8", 8, 9);

        verifyMixedTestResult(result, "greeter.provider-jdk8", 8, 9);
        verifyMixedTestResult(result, "greeter.provider-jdk8.test-jdk8", 8, 9);
        verifyMixedTestResult(result, "greeter.provider-jdk8.test-jdk11", 11, 11);

        verifyMixedTestResult(result, "greeter.provider-jdk11", 11, 11);
        verifyMixedTestResult(result, "greeter.provider-jdk11.test-jdk11", 11, 11);
    }

    private static void verifyMixedTestResult(
            BuildResult result, String subprojectName,
            int mainJavaRelease, int moduleInfoJavaRelease) throws IOException {
        assertTasksSuccessful(result, subprojectName, "build");
        assertExpectedClassFileFormats(subprojectName, mainJavaRelease, moduleInfoJavaRelease);
    }

    private static void assertExpectedClassFileFormats(
            String subprojectName, int mainJavaRelease, int moduleInfoJavaRelease) throws IOException {
        Path basePath = Path.of("test-project-mixed").resolve(subprojectName).resolve("build/classes");
        Path classesDir = basePath.resolve("java/main");
        Path moduleInfoClassesDir = basePath.resolve("module-info");

        List<Path> moduleInfoPaths = Stream.of(classesDir, moduleInfoClassesDir)
                .map(dir -> dir.resolve("module-info.class"))
                .filter(path -> path.toFile().isFile())
                .collect(Collectors.toList());
        assertEquals(1, moduleInfoPaths.size(), "module-info.class found in multiple locations: " + moduleInfoPaths);
        Path moduleInfoClassPath = moduleInfoPaths.get(0);
        SmokeTestHelper.assertClassFileJavaVersion(moduleInfoJavaRelease, moduleInfoClassPath);

        Path nonModuleInfoClassPath = SmokeTestHelper.anyNonModuleInfoClassFilePath(classesDir);
        SmokeTestHelper.assertClassFileJavaVersion(mainJavaRelease, nonModuleInfoClassPath);
    }

    @ParameterizedTest
    @ValueSource(strings = "test-project")
    void smokeTestDist(String projectName) {
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(GRADLE_VERSION)
                .withArguments("-c", "smoke_test_settings.gradle", "clean", "build", ":greeter.runner:installDist", "--stacktrace")
                .forwardOutput()
                .build();

        assertTasksSuccessful(result, "greeter.runner", "installDist");
        Path installDir = Path.of(projectName + "/greeter.runner/build/install/greeter.runner");
        assertTrue(installDir.toFile().exists(), "Install dir was not created");

        Path libDir = installDir.resolve("lib");
        Path patchlibsDir = installDir.resolve("patchlibs");

        assertTrue(libDir.toFile().exists(), "Lib dir was not created");
        assertTrue(patchlibsDir.toFile().exists(), "Patchlib dir was not created");

        Path patchedLib = patchlibsDir.resolve("jsr305-3.0.2.jar");
        assertTrue(patchedLib.toFile().exists(), "Patched lib should be in patchlibs dir");

        assertEquals(0, libDir.toFile().listFiles(f -> f.getName().equals("jsr305-3.0.2.jar")).length, "Patched libs should not be in lib dir");
        assertEquals(4, libDir.toFile().listFiles().length, "Unexpected number of jars in lib dir");

        SmokeTestAppContext ctx = SmokeTestAppContext.ofDefault(installDir.resolve("bin"));
        assertTrue(ctx.getAppOutput("greeter.runner").contains("welcome"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test-project", "test-project-kotlin", "test-project-groovy"})
    void smokeTestRunDemo(String projectName) {
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(GRADLE_VERSION)
                .withArguments("-c", "smoke_test_settings.gradle", "clean", "build",
                        ":greeter.javaexec:runDemo1", ":greeter.javaexec:runDemo2", "--info", "--stacktrace")
                .forwardOutput()
                .build();

        assertTasksSuccessful(result, "greeter.javaexec", "runDemo1", "runDemo2");
    }

    @ParameterizedTest
    @ValueSource(strings = {"test-project", "test-project-kotlin", "test-project-groovy"})
    void smokeTestRunStartScripts(String projectName) {
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(GRADLE_VERSION)
                .withArguments("-c", "smoke_test_settings.gradle", "clean", ":greeter.startscripts:installDist", "--info", "--stacktrace")
                .forwardOutput()
                .build();

        assertTasksSuccessful(result, "greeter.startscripts", "installDist");

        String binDir = projectName + "/greeter.startscripts/build/install/demo/bin";
        SmokeTestAppContext ctx = SmokeTestAppContext.ofAliceAndBobAtHome(Path.of(binDir));

        assertEquals("MainDemo: welcome home, Alice and Bob!", ctx.getAppOutput("demo"));
        assertEquals("Demo1: welcome home, Alice and Bob!", ctx.getAppOutput("demo1"));
        assertEquals("Demo2: welcome home, Alice and Bob!", ctx.getAppOutput("demo2"));
    }

    private static void assertTasksSuccessful(BuildResult result, String subprojectName, String... taskNames) {
        for (String taskName : taskNames) {
            SmokeTestHelper.assertTaskSuccessful(result, subprojectName, taskName);
        }
    }

}
