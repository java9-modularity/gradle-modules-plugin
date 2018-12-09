package org.javamodularity.moduleplugin;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("ConstantConditions")
class ModulePluginSmokeTest {

    private List<File> pluginClasspath;

    @BeforeEach
    void before() throws IOException {
        pluginClasspath = Resources.readLines(Resources.getResource("plugin-classpath.txt"), Charsets.UTF_8)
                .stream()
                .map(fname -> new File(fname))
                .collect(Collectors.toList());
    }

    @ParameterizedTest
    @ValueSource(strings = { "test-project", "test-project-kotlin" })
    void smokeTest(String projectName) {
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion("4.10.2")
                .withArguments("-c", "smoke_test_settings.gradle", "clean", "build", "run", "--stacktrace")
                .forwardOutput()
                .build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":greeter.api:build").getOutcome(), "Failed Build!");
        assertEquals(TaskOutcome.SUCCESS, result.task(":greeter.provider:build").getOutcome(), "Failed Build!");
        assertEquals(TaskOutcome.SUCCESS, result.task(":greeter.provider.test:build").getOutcome(), "Failed Build!");
        assertEquals(TaskOutcome.SUCCESS, result.task(":greeter.runner:build").getOutcome(), "Failed Build!");
        assertEquals(TaskOutcome.SUCCESS, result.task(":greeter.runner:run").getOutcome(), "Failed Build!");
    }

    @ParameterizedTest
    @ValueSource(strings = { "test-project", "test-project-kotlin" })
    void smokeTestRunDemo(String projectName) {
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion("4.10.2")
                .withArguments("-c", "smoke_test_settings.gradle", "clean", "build",
                        ":greeter.javaexec:runDemo1", ":greeter.javaexec:runDemo2", "--info", "--stacktrace")
                .forwardOutput()
                .build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":greeter.javaexec:runDemo1").getOutcome(), "Failed Build!");
        assertEquals(TaskOutcome.SUCCESS, result.task(":greeter.javaexec:runDemo2").getOutcome(), "Failed Build!");
    }

    @ParameterizedTest
    @ValueSource(strings = {"test-project", "test-project-kotlin"})
    void smokeTestRunStartScripts(String projectName) {
        var result = GradleRunner.create()
                .withProjectDir(new File(projectName + "/"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion("4.10.2")
                .withArguments("-c", "smoke_test_settings.gradle", "clean", ":greeter.startscripts:installDist", "--info", "--stacktrace")
                .forwardOutput()
                .build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":greeter.startscripts:installDist").getOutcome(), "Failed Build!");

        String binDir = projectName + "/greeter.startscripts/build/install/demo/bin";
        assertEquals("MainDemo: welcome", getAppOutput(binDir, "demo"));
        assertEquals("Demo1: welcome", getAppOutput(binDir, "demo1"));
        assertEquals("Demo2: welcome", getAppOutput(binDir, "demo2"));
    }

    private static String getAppOutput(String binDirPath, String appName) {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("windows");
        String scriptName = windows ? (appName + ".bat") : appName ;

        File binDir = new File(binDirPath).getAbsoluteFile();
        Process process;
        try {
            process = new ProcessBuilder()
                    .directory(binDir)
                    .command(new File(binDir, scriptName).getPath())
                    .start();
            process.waitFor(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        if(process.exitValue() != 0) {
            String errText = getText(process.getErrorStream());
            System.err.println("Process terminated with exit code " + process.exitValue() + ": " + errText);
            return errText;
        }
        return getText(process.getInputStream());
    }

    public static String getText(InputStream inputStream){
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        } catch(IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
