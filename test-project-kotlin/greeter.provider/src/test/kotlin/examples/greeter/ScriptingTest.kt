package examples.greeter

import javax.script.*
import org.junit.jupiter.api.*

class ScriptingTest {
    @Test
    fun testScripting() {
        val manager = ScriptEngineManager()
        Assertions.assertNotNull(manager.getEngineFactories())
    }
}
