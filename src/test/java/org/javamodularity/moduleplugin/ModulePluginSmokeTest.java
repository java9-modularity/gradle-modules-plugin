package org.javamodularity.moduleplugin;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.cartesian.CartesianTest;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SuppressWarnings("ConstantConditions")
class ModulePluginSmokeTest {
    private static final Logger LOGGER = Logging.getLogger(ModulePluginSmokeTest.class);
    private static final Pattern SEMANTIC_VERSION = Pattern.compile("(?<major>\\d+)\\.(?<minor>\\d+).(?<patch>\\d+)");

    private List<File> pluginClasspath;

    @SuppressWarnings("unused")
    private enum GradleVersion {
        v8_11, v8_14_3,
        v9_1_0
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

    @CartesianTest(name = "smokeTest({arguments})")
    void smokeTest(
            @CartesianTest.Values(strings = {
                    "test-project",
                    "test-project-kotlin-pre-1-7",
                    "test-project-kotlin",
                    "test-project-groovy"
            }) String projectName,
            @CartesianTest.Enum GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTest of {} with Gradle {}", projectName, gradleVersion);
        assumeTrue(jdkSupported(gradleVersion));
        assumeTrue(checkKotlinCombination(projectName, gradleVersion));
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
                .withArguments("clean", "build", "run", "--stacktrace")
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

    @CartesianTest(name = "smokeTestRun({arguments})")
    void smokeTestRun(
            @CartesianTest.Values(strings = {
                    "test-project",
                    "test-project-kotlin-pre-1-7",
                    "test-project-kotlin",
                    "test-project-groovy"
            }) String projectName,
            @CartesianTest.Enum GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTestRun of {} with Gradle {}", projectName, gradleVersion);
        assumeTrue(jdkSupported(gradleVersion));
        assumeTrue(checkKotlinCombination(projectName, gradleVersion));
        var writer = new StringWriter(256);
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
                .withArguments("-q", "clean", ":greeter.runner:run", "--args", "aaa bbb")
                .forwardStdOutput(writer)
                .forwardStdError(writer)
                .build();

        assertTasksSuccessful(result, "greeter.runner", "run");

        var lines = writer.toString().lines().collect(Collectors.toList());
        assertEquals("args: [aaa, bbb]", lines.get(0));
        assertEquals("greeter.sender: gradle-modules-plugin", lines.get(1));
        assertEquals("welcome", lines.get(2));
    }

    @CartesianTest(name = "smokeTestJunit5({arguments})")
    void smokeTestJunit5(
            @CartesianTest.Values(strings = {
                    "5.4.2/1.4.2",
                    "5.5.2/1.5.2",
                    "5.7.1/1.7.1",
                    "5.8.0/1.8.0",
                    "5.10.2/1.10.2"
            }) String junitVersionPair,
            @CartesianTest.Enum GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTestJunit5 with junitVersionPair {} and Gradle {}", junitVersionPair, gradleVersion);
        assumeTrue(jdkSupported(gradleVersion));
        var junitVersionParts = junitVersionPair.split("/");
        final String junitVersion = junitVersionParts[0];
        assumeTrue(checkJUnitCombination(junitVersion, gradleVersion));
        var junitVersionProperty = String.format("-PjUnitVersion=%s", junitVersion);
        var junitPlatformVersionProperty = String.format("-PjUnitPlatformVersion=%s", junitVersionParts[1]);
        var result = GradleRunner.create()
                .withProjectDir(new File("test-project/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
                .withArguments(junitVersionProperty, junitPlatformVersionProperty, "clean", "build", "run", "--stacktrace")
                .forwardOutput()
                .build();

        assertTasksSuccessful(result, "greeter.api", "build");
        assertTasksSuccessful(result, "greeter.provider", "build");
        assertTasksSuccessful(result, "greeter.provider.test", "build");
        assertTasksSuccessful(result, "greeter.runner", "build", "run");
    }

    @CartesianTest(name = "smokeTestMixed({arguments})")
    void smokeTestMixed(@CartesianTest.Enum GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTestMixed with Gradle {}", gradleVersion);
        assumeTrue(jdkSupported(gradleVersion));
        var result = GradleRunner.create()
                .withProjectDir(new File("test-project-mixed"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
                .withArguments("clean", "build", "--stacktrace")
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

    @CartesianTest(name = "smokeTestDist({arguments})")
    void smokeTestDist(
            @CartesianTest.Values(strings = {
                    "test-project",
                    "test-project-kotlin-pre-1-7",
                    "test-project-kotlin",
                    "test-project-groovy"
            }) String projectName,
            @CartesianTest.Enum GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTestDist of {} with Gradle {}", projectName, gradleVersion);
        assumeTrue(jdkSupported(gradleVersion));
        assumeTrue(checkKotlinCombination(projectName, gradleVersion));
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
                .withArguments("clean", "build", ":greeter.runner:installDist", "--stacktrace")
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

    @CartesianTest(name = "smokeTestRunDemo({arguments})")
    void smokeTestRunDemo(
            @CartesianTest.Values(strings = {
                    "test-project",
                    "test-project-kotlin-pre-1-7",
                    "test-project-kotlin",
                    "test-project-groovy"
            }) String projectName,
            @CartesianTest.Enum GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTestRunDemo of {} with Gradle {}", projectName, gradleVersion);
        assumeTrue(jdkSupported(gradleVersion));
        assumeTrue(checkKotlinCombination(projectName, gradleVersion));
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
                .withArguments("clean", "build",
                        ":greeter.javaexec:runDemo1", ":greeter.javaexec:runDemo2", "--info", "--stacktrace")
                .forwardOutput()
                .build();

        assertTasksSuccessful(result, "greeter.javaexec", "runDemo1", "runDemo2");
        assertFalse(result.getOutput().contains("Using Java lambdas is not supported as task inputs"));
    }

    @CartesianTest(name = "smokeTestRunStartScripts({arguments})")
    void smokeTestRunStartScripts(
            @CartesianTest.Values(strings = {
                    "test-project",
                    "test-project-kotlin-pre-1-7",
                    "test-project-kotlin",
                    "test-project-groovy"
            }) String projectName,
            @CartesianTest.Enum GradleVersion gradleVersion) {
        LOGGER.lifecycle("Executing smokeTestRunScripts of {} with Gradle {}", projectName, gradleVersion);
        assumeTrue(jdkSupported(gradleVersion));
        assumeTrue(checkKotlinCombination(projectName, gradleVersion));
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion.toString())
                .withArguments("clean", ":greeter.startscripts:installDist", "--info", "--stacktrace")
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
        assertEquals(2, GradleVersion.values().length);
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

    private static boolean checkKotlinCombination(String projectName, GradleVersion gradleVersion) {
        final boolean kotlin_NotSupported = projectName.startsWith("test-project-kotlin") && gradleVersion.toString().compareTo("6.4") < 0;
        final boolean kotlinPost1_7_NotSupported = projectName.equals("test-project-kotlin") && gradleVersion.toString().compareTo("6.6") < 0;
        final boolean kotlinPre1_7_NotSupported = projectName.equals("test-project-kotlin-pre-1-7") && gradleVersion.toString().compareTo("8.0") >= 0;
        if (kotlin_NotSupported || kotlinPost1_7_NotSupported || kotlinPre1_7_NotSupported) {
            LOGGER.lifecycle("Unsupported combination: {} / Gradle {}. Test skipped", projectName, gradleVersion);
            return false;
        }
        return true;
    }

    private boolean checkJUnitCombination(final String junitVersion, final GradleVersion gradleVersion) {
        final boolean gradleEighthPlus = gradleVersion.ordinal() >= GradleVersion.v8_11.ordinal();
        final Matcher m = SEMANTIC_VERSION.matcher(junitVersion);
        assumeTrue(m.matches(), "JUnit version not semantic: " + junitVersion);
        final boolean junitOlderThan5_8_0 = Integer.parseInt(m.group("major")) < 5 ||
                (Integer.parseInt(m.group("major")) == 5 && Integer.parseInt(m.group("minor")) < 8);

        if (gradleEighthPlus && junitOlderThan5_8_0) {
            LOGGER.lifecycle("Unsupported JUnit and Gradle combination. Gradle: {}, JUnit: {}: Test skipped", gradleVersion, junitVersion);
            return false;
        }
        return true;
    }

    private static int javaMajorVersion() {
        final String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            // Java 8 and earlier (1.8.0_xxx format) - not supported anymore but keep for completeness
            return Integer.parseInt(version.substring(2, version.indexOf(".", 2)));
        } else {
            // Java 9+ (9.0.1, 11.0.2, 17.0.2 format)
            int dotIndex = version.indexOf(".");
            if (dotIndex == -1) {
                // Handle cases like "17" without dot
                return Integer.parseInt(version);
            }
            return Integer.parseInt(version.substring(0, dotIndex));
        }

    }

    private boolean jdkSupported(final GradleVersion gradleVersion) {
        final int javaMajor = javaMajorVersion();

        // All supported Gradle versions (8.11+) require Java 17+
        if (javaMajor < 17) {
            LOGGER.lifecycle("Gradle {} requires Java 17+, but running Java {}: Test skipped", gradleVersion, javaMajor);
            return false;
        }

        return true;
    }
}
