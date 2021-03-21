package testfixture;

import examples.greeter.api.Greeter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class GreeterTest extends GreeterFixture {
    @Test
    void testLocate() {
        Greeter greeter = locator().findGreeter();
        assertFalse(greeter.hello().isBlank());
    }
}
