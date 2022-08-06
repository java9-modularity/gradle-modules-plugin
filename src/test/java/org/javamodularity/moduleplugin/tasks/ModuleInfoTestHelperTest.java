package org.javamodularity.moduleplugin.tasks;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ModuleInfoTestHelperTest {

    private final List<String> output = new ArrayList<>();

    @Test
    void shouldFilterOutBlankLines() {
        final Stream<String> lines = Stream.of("", " ", "\t");

        ModuleInfoTestHelper.consumeLines(lines, false, output::add);

        assertEquals(List.of(), output);
    }

    @Test
    void shouldFilterOutCommentLines() {
        final Stream<String> lines = Stream.of("// comment", " // indented comment");

        ModuleInfoTestHelper.consumeLines(lines, false, output::add);

        assertEquals(List.of(), output);
    }

    @Test
    void shouldTrimLines() {
        final Stream<String> lines = Stream.of(" a ", "\tb\t");

        ModuleInfoTestHelper.consumeLines(lines, false, output::add);

        assertEquals(List.of("a", "b"), output);
    }

    @Test
    void shouldNotExcludeOpens() {
        final Stream<String> lines = Stream.of("a", "--add-opens", "c");

        ModuleInfoTestHelper.consumeLines(lines, false, output::add);

        assertEquals(List.of("a", "--add-opens", "c"), output);
    }

    @Test
    void shouldExcludeOpens() {
        final Stream<String> lines = Stream.of("a", " --add-opens", "x=y", "c");

        ModuleInfoTestHelper.consumeLines(lines, true, output::add);

        assertEquals(List.of("a", "c"), output);
    }

    @Test
    void shouldNotBlowUpOnExcludedOpenAtEnd() {
        final Stream<String> lines = Stream.of("a", "--add-opens");

        ModuleInfoTestHelper.consumeLines(lines, true, output::add);

        assertEquals(List.of("a"), output);
    }
}