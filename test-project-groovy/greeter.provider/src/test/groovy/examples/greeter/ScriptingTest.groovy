package examples.greeter

import javax.script.*
import org.junit.jupiter.api.*

class ScriptingTest {
    @Test
    void testScripting() {
        def manager = new ScriptEngineManager()
        Assertions.assertNotNull(manager.getEngineFactories())
    }
}
