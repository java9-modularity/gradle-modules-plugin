package examples.greeter;

import javax.script.*;
import org.junit.jupiter.api.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ScriptingTest {

    @Test
    void testScripting() {
        ScriptEngineManager manager = new ScriptEngineManager();
        assertThat(manager.getEngineFactories(), not(nullValue()));
    }
}
