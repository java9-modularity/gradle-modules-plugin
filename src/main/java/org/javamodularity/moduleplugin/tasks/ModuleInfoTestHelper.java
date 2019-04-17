package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.javamodularity.moduleplugin.JavaProjectHelper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.function.Consumer;

class ModuleInfoTestHelper {

    private static final Logger LOGGER = Logging.getLogger(ModuleInfoTestHelper.class);

    static void mutateArgs(Project project, Consumer<String> consumer) {
        JavaProjectHelper helper = new JavaProjectHelper(project);

        String moduleName = helper.moduleName();
        var testSourceSet = helper.testSourceSet();

        var files = testSourceSet.getAllSource().matching(f -> f.include("module-info.test"));
        if (files.isEmpty()) {
            LOGGER.debug("File 'module-info.test' is not present in {}", project);
            return;
        }

        var moduleInfoTestPath = files.getSingleFile().toPath();
        LOGGER.debug("Using lines of '{}' to patch module {}...", moduleInfoTestPath, moduleName);
        try (var lines = Files.lines(moduleInfoTestPath)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("//"))
                    .peek(line -> LOGGER.debug("  {}", line))
                    .forEach(consumer);
        } catch (IOException e) {
            throw new UncheckedIOException("Reading " + moduleInfoTestPath + " failed", e);
        }
    }
}
