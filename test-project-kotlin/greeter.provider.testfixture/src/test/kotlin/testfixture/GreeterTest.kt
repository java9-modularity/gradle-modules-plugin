package testfixture

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertFalse

class GreeterTest: GreeterFixture() {
    @Test
    fun testLocate() {
        val greeter = locator().findGreeter()
        assertFalse(greeter.hello().isBlank())
    }
}
