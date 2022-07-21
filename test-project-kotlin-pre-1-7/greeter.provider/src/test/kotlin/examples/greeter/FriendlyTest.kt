package examples.greeter

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class FriendlyTest {
    @Test
    fun testGreeting() {
        val greeting = Friendly().hello()
        assertTrue(greeting.contains("welcome"))
    }
}
