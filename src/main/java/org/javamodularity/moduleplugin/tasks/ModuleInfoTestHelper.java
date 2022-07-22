package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.javamodularity.moduleplugin.JavaProjectHelper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ModuleInfoTestHelper {

    private static final Logger LOGGER = Logging.getLogger(ModuleInfoTestHelper.class);

    static void mutateArgs(Project project, boolean excludeOpens, Consumer<String> consumer) {
        JavaProjectHelper helper = new JavaProjectHelper(project);

        String moduleName = helper.moduleName();
        var testSourceSet = helper.testSourceSet();

        var files = testSourceSet.getAllSource().matching(f -> f.include("module-info.test"));
        if (files.isEmpty()) {
            LOGGER.info("File 'module-info.test' is not present in {}", project);
            return;
        }

        var moduleInfoTestPath = files.getSingleFile().toPath();
        LOGGER.info("Using lines of '{}' to patch module {}...", moduleInfoTestPath, moduleName);
        try (var lines = Files.lines(moduleInfoTestPath)) {
            consumeLines(lines, excludeOpens, consumer);
        } catch (IOException e) {
            throw new UncheckedIOException("Reading " + moduleInfoTestPath + " failed", e);
        }
    }

    // Visible for testing.
    static void consumeLines(Stream<String> s, boolean excludeOpens, Consumer<String> consumer) {
        final List<String> lines = s.map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.startsWith("//"))
                .collect(Collectors.toList());

        if (excludeOpens) {
            excludeOpens(lines);
        }

        lines.stream()
                .peek(line -> LOGGER.debug("  {}", line))
                .forEach(consumer);
    }

    private static void excludeOpens(final List<String> lines) {
        final Iterator<String> it = lines.iterator();
        while (it.hasNext()) {
            final String next = it.next();
            if (!next.equals("--add-opens")) {
                continue;
            }

            it.remove();
            if (it.hasNext()) {
                final String opens = it.next();
                LOGGER.debug("  Excluding --add-opens {}", opens);
                it.remove();
            }
        }
    }
}
