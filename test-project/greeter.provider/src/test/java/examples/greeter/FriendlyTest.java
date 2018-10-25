package examples.greeter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FriendlyTest {
    @Test
    void testGreeting() {
        String greeting = new Friendly().hello();
        assertTrue(greeting.contains("welcome"));
    }

}