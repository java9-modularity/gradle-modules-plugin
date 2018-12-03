package tests

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertFalse

class GreeterTest {
    @Test
    fun testLocate() {
        val greeter = GreeterLocator().findGreeter()
        assertFalse(greeter.hello().isBlank())
    }
}
