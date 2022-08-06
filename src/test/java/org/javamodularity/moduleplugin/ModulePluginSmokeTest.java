package org.javamodularity.moduleplugin;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.CartesianEnumSource;
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

/**
 * If you're locally seeing the error:
 * {@code Could not create an instance of type org.gradle.initialization.DefaultSettings_Decorated.}
 * when running these test it will because you're using a recent version of Java and running into:
 * https://github.com/gradle/gradle/issues/10248.
 *
 * <p>Either switch to pre JDK-14 or <b>locally</b> comment out v5_1 and v5_2 in the {@link GradleVersion} enum.
 */
@SuppressWarnings("ConstantConditions")
class ModulePluginSmokeTest {
    private static final Logger LOGGER = Logging.getLogger(ModulePluginSmokeTest.class);

    private List<File> pluginClasspath;

    @SuppressWarnings("unused")
    private enum GradleVersion {
        // Locally comment out the 5.x versions if running JDK-14+
        v5_1, v5_6,
        v6_3, v6_4_1, v6_5_1, v6_8_3,
        v7_0, v7_2
        ;

        @Override
        public String toString() {
            return  name().substring(1).replaceAll("_", ".");
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @BeforeEach
    void before() throws IOException {
        pluginClasspath = Resources.readLines(Resources.getResource("plugin-classpath.txt"), Charsets.UTF_8)
                .stream()
                .map(File::new)
                .collect(Collectors.toList());
    }

    @CartesianProductTest(name = "smokeTest({arguments})")
    @CartesianValueSource(strings = {"test-project", "test-project-kotlin-pre-1-7", "test-project-kotlin", "test-project-groovy"})
    @CartesianEnumSource(GradleVersion.class)
    void smokeTest(String projectName, GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTest of {} with Gradle {}", projectName, gradleVersion);
        if(!checkCombination(projectName, gradleVersion)) return;
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
                .withArguments("-c", "smoke_test_settings.gradle", "clean", "build", "run", "--stacktrace")
                .forwardOutput()
                .build();

        assertTasksSuccessful(result, "greeter.api", "build");
        assertTasksSuccessful(result, "greeter.provider", "build");
        assertTasksSuccessful(result, "greeter.provider.test", "build");
        if(org.gradle.util.GradleVersion.version(gradleVersion.toString()).compareTo(org.gradle.util.GradleVersion.version("5.6")) >= 0) {
            assertTasksSuccessful(result, "greeter.provider.testfixture", "build");
        }
        assertTasksSuccessful(result, "greeter.runner", "build", "run");
        assertOutputDoesNotContain(result, "warning: [options] --add-opens has no effect at compile time");
    }

    @CartesianProductTest(name = "smokeTestRun({arguments})")
    @CartesianValueSource(strings = {"test-project", "test-project-kotlin-pre-1-7", "test-project-kotlin", "test-project-groovy"})
    @CartesianEnumSource(GradleVersion.class)
    void smokeTestRun(String projectName, GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTestRun of {} with Gradle {}", projectName, gradleVersion);
        if(!checkCombination(projectName, gradleVersion)) return;
        var writer = new StringWriter(256);
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
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
    @CartesianEnumSource(GradleVersion.class)
    void smokeTestJunit5(String junitVersionPair, GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTestJunit5 with junitVersionPair {} and Gradle {}", junitVersionPair, gradleVersion);
        var junitVersionParts = junitVersionPair.split("/");
        var junitVersionProperty = String.format("-PjUnitVersion=%s", junitVersionParts[0]);
        var junitPlatformVersionProperty = String.format("-PjUnitPlatformVersion=%s", junitVersionParts[1]);
        var result = GradleRunner.create()
                .withProjectDir(new File("test-project/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
                .withArguments("-c", "smoke_test_settings.gradle", junitVersionProperty, junitPlatformVersionProperty, "clean", "build", "run", "--stacktrace")
                .forwardOutput()
                .build();

        assertTasksSuccessful(result, "greeter.api", "build");
        assertTasksSuccessful(result, "greeter.provider", "build");
        assertTasksSuccessful(result, "greeter.provider.test", "build");
        assertTasksSuccessful(result, "greeter.runner", "build", "run");
    }

    @CartesianProductTest(name = "smokeTestMixed({arguments})")
    @CartesianEnumSource(GradleVersion.class)
    void smokeTestMixed(GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTestMixed with Gradle {}", gradleVersion);
        var result = GradleRunner.create()
                .withProjectDir(new File("test-project-mixed"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
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
    @CartesianValueSource(strings = {"test-project", "test-project-kotlin-pre-1-7", "test-project-kotlin", "test-project-groovy"})
    @CartesianEnumSource(GradleVersion.class)
    void smokeTestDist(String projectName, GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTestDist of {} with Gradle {}", projectName, gradleVersion);
        if(!checkCombination(projectName, gradleVersion)) return;
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
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
    @CartesianValueSource(strings = {"test-project", "test-project-kotlin-pre-1-7", "test-project-kotlin", "test-project-groovy"})
    @CartesianEnumSource(GradleVersion.class)
    void smokeTestRunDemo(String projectName, GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTestRunDemo of {} with Gradle {}", projectName, gradleVersion);
        if(!checkCombination(projectName, gradleVersion)) return;
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
                .withArguments("-c", "smoke_test_settings.gradle", "clean", "build",
                        ":greeter.javaexec:runDemo1", ":greeter.javaexec:runDemo2", "--info", "--stacktrace")
                .forwardOutput()
                .build();

        assertTasksSuccessful(result, "greeter.javaexec", "runDemo1", "runDemo2");
        assertFalse(result.getOutput().contains("Using Java lambdas is not supported as task inputs"));
    }

    @CartesianProductTest(name = "smokeTestRunStartScripts({arguments})")
    @CartesianValueSource(strings = {"test-project", "test-project-kotlin-pre-1-7", "test-project-kotlin", "test-project-groovy"})
    @CartesianEnumSource(GradleVersion.class)
    void smokeTestRunStartScripts(String projectName, GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTestRunScripts of {} with Gradle {}", projectName, gradleVersion);
        if(!checkCombination(projectName, gradleVersion)) return;
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
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

    @Test
    void shouldNotCheckInWithCommentedOutVersions() {
        assertEquals(8, GradleVersion.values().length);
    }

    private static void assertTasksSuccessful(BuildResult result, String subprojectName, String... taskNames) {
        for (String taskName : taskNames) {
            SmokeTestHelper.assertTaskSuccessful(result, subprojectName, taskName);
        }
    }

    private static void assertOutputDoesNotContain(BuildResult result, String text) {
        final String output = result.getOutput();
        assertFalse(output.contains(text), "Output should not contain '" + text + "', but was: " + output);
    }

    private static boolean checkCombination(String projectName, GradleVersion gradleVersion) {
        final boolean kotlin_NotSupported = projectName.startsWith("test-project-kotlin") && gradleVersion.toString().compareTo("6.4") < 0;
        final boolean kotlin1_7_NotSupported = projectName.equals("test-project-kotlin") && gradleVersion.toString().compareTo("6.6") < 0;
        if (kotlin_NotSupported || kotlin1_7_NotSupported) {
            LOGGER.lifecycle("Unsupported combination: {} / Gradle {}. Test skipped", projectName, gradleVersion);
            return false;
        }
        return true;
    }
}
