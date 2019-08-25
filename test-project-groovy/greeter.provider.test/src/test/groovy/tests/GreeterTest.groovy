package tests

import examples.greeter.api.Greeter
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertFalse

class GreeterTest {
    @Test
    void testLocate() {
        Greeter greeter = new GreeterLocator().findGreeter()
        assertFalse(greeter.hello().blank)
    }
}
