package examples.greeter;

import javax.script.*;
import org.junit.jupiter.api.*;

class ScriptingTest {

    @Test
    void testScripting() {
        ScriptEngineManager manager = new ScriptEngineManager();
        Assertions.assertNotNull(manager.getEngineFactories());
    }
}
