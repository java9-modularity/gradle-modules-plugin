package examples.greeter;

import java.lang.module.ModuleReader;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleReaderTest {
    @Test
    void testList() throws Exception {
        ModuleReader reader = ModuleReaderTest.class.getModule()
                .getLayer()
                .configuration()
                .findModule("greeter.provider")
                .orElseThrow()
                .reference()
                .open();

        List<String> content = reader.list().collect(Collectors.toList());

        assertFalse(content.isEmpty());
    }
}