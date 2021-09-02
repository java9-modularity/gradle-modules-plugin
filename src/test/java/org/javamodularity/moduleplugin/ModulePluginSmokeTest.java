package org.javamodularity.moduleplugin;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junitpioneer.jupiter.CartesianProductTest;
import org.junitpioneer.jupiter.CartesianValueSource;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ConstantConditions")
class ModulePluginSmokeTest {
    private static final Logger LOGGER = Logging.getLogger(ModulePluginSmokeTest.class);

    private List<File> pluginClasspath;

    @BeforeEach
    void before() throws IOException {
        pluginClasspath = Resources.readLines(Resources.getResource("plugin-classpath.txt"), Charsets.UTF_8)
                .stream()
                .map(File::new)
                .collect(Collectors.toList());
    }

    @CartesianProductTest(name = "smokeTest({arguments})")
    @CartesianValueSource(strings = {"test-project", "test-project-kotlin", "test-project-groovy"})
    @CartesianValueSource(strings = {"5.1", "5.6", "6.3", "6.4.1", "6.5.1", "6.8.3", "7.0"})
    void smokeTest(String projectName, String gradleVersion) {
        LOGGER.info("Executing smokeTest with Gradle {}", gradleVersion);
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion)
                .withArguments("-c", "smoke_test_settings.gradle", "clean", "build", "run", "--stacktrace")
                .forwardOutput()
                .build();

        assertTasksSuccessful(result, "greeter.api", "build");
        assertTasksSuccessful(result, "greeter.provider", "build");
        assertTasksSuccessful(result, "greeter.provider.test", "build");
        if(GradleVersion.version(gradleVersion).compareTo(GradleVersion.version("5.6")) >= 0) {
            assertTasksSuccessful(result, "greeter.provider.testfixture", "build");
        }
        assertTasksSuccessful(result, "greeter.runner", "build", "run");
    }

    @CartesianProductTest(name = "smokeTestRun({arguments})")
    @CartesianValueSource(strings = {"test-project", "test-project-kotlin", "test-project-groovy"})
    // Fails with Gradle versions >= 6.6. See https://github.com/java9-modularity/gradle-modules-plugin/issues/165
    @CartesianValueSource(strings = {"5.1", "5.6", "6.3", "6.4.1", "6.5.1"/*, "6.8.3", "7.0"*/})
    void smokeTestRun(String projectName, String gradleVersion) {
        LOGGER.info("Executing smokeTestRun with Gradle {}", gradleVersion);
        var writer = new StringWriter(256);
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion)
                .withArguments("-q", "-c", "smoke_test_settings.gradle", "clean", ":greeter.runner:run", "--args", "aaa bbb")
                .forwardStdOutput(writer)
                .forwardStdError(writer)
                .build();

        assertTasksSuccessful(result, "greeter.runner", "run");

        var lines = writer.toString().lines().collect(Collectors.toList());
        assertEquals("args: [aaa, bbb]", lines.get(0));
        assertEquals("greeter.sender: gradle-modules-plugin", lines.get(1));
        assertEquals("welcome", lines.get(2));
    }


    @CartesianProductTest(name = "smokeTestJunit5({arguments})")
    @CartesianValueSource(strings = {"5.4.2/1.4.2", "5.5.2/1.5.2", "5.7.1/1.7.1"})
    @CartesianValueSource(strings = {"5.1", "5.6", "6.3", "6.4.1", "6.5.1", "6.8.3", "7.0"})
    void smokeTestJunit5(String junitVersionPair, String gradleVersion) {
        LOGGER.info("Executing smokeTestJunit5 with Gradle {}", gradleVersion);
        var junitVersionParts = junitVersionPair.split("/");
        var junitVersionProperty = String.format("-PjUnitVersion=%s", junitVersionParts[0]);
        var junitPlatformVersionProperty = String.format("-PjUnitPlatformVersion=%s", junitVersionParts[1]);
        var result = GradleRunner.create()
                .withProjectDir(new File("test-project/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion)
                .withArguments("-c", "smoke_test_settings.gradle", junitVersionProperty, junitPlatformVersionProperty, "clean", "build", "run", "--stacktrace")
                .forwardOutput()
                .build();

        assertTasksSuccessful(result, "greeter.api", "build");
        assertTasksSuccessful(result, "greeter.provider", "build");
        assertTasksSuccessful(result, "greeter.provider.test", "build");
        assertTasksSuccessful(result, "greeter.runner", "build", "run");
    }

    @CartesianProductTest(name = "smokeTestMixed({arguments})")
    // It currently fails with Gradle 7.0
    @CartesianValueSource(strings = {"5.1", "5.6", "6.3", "6.4.1", "6.5.1", "6.8.3"/*, "7.0"*/})
    void smokeTestMixed(String gradleVersion) {
        LOGGER.info("Executing smokeTestMixed with Gradle {}", gradleVersion);
        var result = GradleRunner.create()
                .withProjectDir(new File("test-project-mixed"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion)
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
            int mainJavaRelease, int moduleInfoJavaRelease) {
        assertTasksSuccessful(result, subprojectName, "build");
        assertExpectedClassFileFormats(subprojectName, mainJavaRelease, moduleInfoJavaRelease);
    }

    private static void assertExpectedClassFileFormats(
            String subprojectName, int mainJavaRelease, int moduleInfoJavaRelease) {
        Path basePath = Path.of("test-project-mixed").resolve(subprojectName).resolve("build/classes");
        Path classesDir = basePath.resolve("java/main");
        Path moduleInfoClassesDir = basePath.resolve("module-info");

        List<Path> moduleInfoPaths = Stream.of(classesDir, moduleInfoClassesDir)
                .map(dir -> dir.resolve("module-info.class"))
                .filter(path -> path.toFile().isFile())
                .collect(Collectors.toList());
        assertEquals(1, moduleInfoPaths.size(), "module-info.class found in multiple locations: " + moduleInfoPaths);
        Path moduleInfoClassPath = moduleInfoPaths.get(0);
        try {
            SmokeTestHelper.assertClassFileJavaVersion(moduleInfoJavaRelease, moduleInfoClassPath);

            Path nonModuleInfoClassPath = SmokeTestHelper.anyNonModuleInfoClassFilePath(classesDir);
            SmokeTestHelper.assertClassFileJavaVersion(mainJavaRelease, nonModuleInfoClassPath);
        } catch (IOException e) {
            fail(e);
        }
    }

    @CartesianProductTest(name = "smokeTestDist({arguments})")
    @CartesianValueSource(strings = {"test-project", "test-project-kotlin", "test-project-groovy"})
    @CartesianValueSource(strings = {"5.1", "5.6", "6.3", "6.4.1", "6.5.1", "6.8.3", "7.0"})
    void smokeTestDist(String projectName, String gradleVersion) {
        LOGGER.info("Executing smokeTestDist with Gradle {}", gradleVersion);
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion)
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

        var libs = Arrays.stream(libDir.toFile().listFiles())
                .map(File::getName)
                .filter(name -> !name.startsWith("kotlin"))
                .filter(name -> !name.startsWith("groovy"))
                .filter(name -> !name.startsWith("annotations"))
                .collect(Collectors.toList());

        assertFalse(libs.contains("jsr305-3.0.2.jar"), "jsr305-3.0.2.jar should not be in libDir");
        assertEquals(4, libs.size(), "Unexpected number of jars in lib dir (" + libs + ")");

        SmokeTestAppContext ctx = SmokeTestAppContext.ofDefault(installDir.resolve("bin"));
        assertTrue(ctx.getAppOutput("greeter.runner").contains("welcome"));
    }

    @CartesianProductTest(name = "smokeTestRunDemo({arguments})")
    @CartesianValueSource(strings = {"test-project", "test-project-kotlin", "test-project-groovy"})
    @CartesianValueSource(strings = {"5.1", "5.6", "6.3", "6.4.1", "6.5.1", "6.8.3", "7.0", "7.2"})
    void smokeTestRunDemo(String projectName, String gradleVersion) {
        LOGGER.info("Executing smokeTestRunDemo with Gradle {}", gradleVersion);
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion)
                .withArguments("-c", "smoke_test_settings.gradle", "clean", "build",
                        ":greeter.javaexec:runDemo1", ":greeter.javaexec:runDemo2", "--info", "--stacktrace")
                .forwardOutput()
                .build();

        assertTasksSuccessful(result, "greeter.javaexec", "runDemo1", "runDemo2");
        assertFalse(result.getOutput().contains("Using Java lambdas is not supported as task inputs"));
    }

    @CartesianProductTest(name = "smokeTestRunStartScripts({arguments})")
    @CartesianValueSource(strings = {"test-project", "test-project-kotlin", "test-project-groovy"})
    // Fails with Gradle versions >= 6.6. See https://github.com/java9-modularity/gradle-modules-plugin/issues/165
    @CartesianValueSource(strings = {"5.1", "5.6", "6.3", "6.4.1", "6.5.1"/*, "6.8.3", "7.0"*/})
    void smokeTestRunStartScripts(String projectName, String gradleVersion) {
        LOGGER.info("Executing smokeTestRunScripts with Gradle {}", gradleVersion);
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion)
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
