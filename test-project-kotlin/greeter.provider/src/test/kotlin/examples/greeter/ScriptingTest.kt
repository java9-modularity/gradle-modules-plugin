package examples.greeter

import javax.script.*
import org.junit.jupiter.api.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*

class ScriptingTest {
    @Test
    fun testScripting() {
        val manager = ScriptEngineManager()
        assertThat(manager.getEngineFactories(), not(nullValue()))
    }
}
