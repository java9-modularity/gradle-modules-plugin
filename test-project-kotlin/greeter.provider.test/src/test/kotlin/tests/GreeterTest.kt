package tests

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertFalse

class GreeterTest {
    @Test
    fun testLocate() {
        val greeter = GreeterLocator().findGreeter()
        assertFalse(greeter.hello().isBlank())
    }

    @Test
    fun testGeneratedResource() {
        val resource = object: Any() {}.javaClass.getResourceAsStream("/generated-resource.txt")
        if (resource == null) {
            throw RuntimeException("Couldn't load generated resource")
        }
    }
}
