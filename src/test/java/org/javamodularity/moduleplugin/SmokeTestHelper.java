package org.javamodularity.moduleplugin;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SmokeTestHelper {

    static void assertTaskSuccessful(BuildResult result, String subprojectName, String taskName) {
        String fullTaskName = String.format(":%s:%s", subprojectName, taskName);
        BuildTask task = Objects.requireNonNull(result.task(fullTaskName), fullTaskName);
        assertEquals(TaskOutcome.SUCCESS, task.getOutcome(), () -> fullTaskName + " failed!");
    }

    //region APP OUTPUT
    static String getAppOutput(String binDirPath, String appName) {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("windows");
        String scriptName = windows ? (appName + ".bat") : appName;

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
        if (process.exitValue() != 0) {
            String errText = getText(process.getErrorStream());
            System.err.println("Process terminated with exit code " + process.exitValue() + ": " + errText);
            return errText;
        }
        return getText(process.getInputStream());
    }

    private static String getText(InputStream inputStream) {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
    //endregion
}
