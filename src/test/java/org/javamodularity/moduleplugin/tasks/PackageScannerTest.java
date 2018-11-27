package org.javamodularity.moduleplugin.tasks;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class PackageScannerTest {
    @ParameterizedTest
    @ValueSource(strings = { "Invalid1", "Invalid2", "Invalid3", "no-such-file" })
    void checkInvalid(String fileName) {
        File f = new File("src/test/resources/scanner/" + fileName + ".java");
        assertNull(new PackageScanner().scan(f));
    }

    @ParameterizedTest
    @CsvSource({"Valid1, example", "Valid2, org.example.test", "Valid2, org.example.test"})
    void checkValid(String fileName, String packageName) {
        File f = new File("src/test/resources/scanner/" + fileName + ".java");
        assertEquals(packageName, new PackageScanner().scan(f));
    }
}
