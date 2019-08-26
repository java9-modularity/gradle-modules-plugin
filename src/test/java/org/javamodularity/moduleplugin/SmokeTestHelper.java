package org.javamodularity.moduleplugin;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SmokeTestHelper {

    static void assertTaskSuccessful(BuildResult result, String subprojectName, String taskName) {
        String fullTaskName = String.format(":%s:%s", subprojectName, taskName);
        BuildTask task = Objects.requireNonNull(result.task(fullTaskName), fullTaskName);
        assertEquals(TaskOutcome.SUCCESS, task.getOutcome(), () -> fullTaskName + " failed!");
    }

    //region CLASS FILE FORMAT
    static void assertClassFileJavaVersion(int expectedJavaVersion, Path classFilePath) throws IOException {
        int actualJavaVersion = classFileJavaVersion(classFilePath);
        assertEquals(expectedJavaVersion, actualJavaVersion, classFilePath::toString);
    }

    static Path anyNonModuleInfoClassFilePath(Path classesDir) throws IOException {
        return Files.walk(classesDir)
                .filter(SmokeTestHelper::isClassFile)
                .filter(path -> !path.endsWith("module-info.class"))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Main class file not found in " + classesDir));
    }

    private static int classFileJavaVersion(Path classFilePath) throws IOException {
        // https://en.wikipedia.org/wiki/Java_class_file#General_layout
        return classFileFormat(classFilePath) - 44;
    }

    private static int classFileFormat(Path classFilePath) throws IOException {
        if (!isClassFile(classFilePath)) {
            throw new IllegalArgumentException(classFilePath.toString());
        }

        try (InputStream inputStream = Files.newInputStream(classFilePath)) {
            // https://en.wikipedia.org/wiki/Java_class_file#General_layout
            return inputStream.readNBytes(8)[7]; // 8th byte: major version number
        }
    }

    private static boolean isClassFile(Path classFilePath) {
        return classFilePath.toString().endsWith(".class");
    }
    //endregion
}
