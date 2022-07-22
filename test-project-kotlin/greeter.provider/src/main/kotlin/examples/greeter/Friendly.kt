package examples.greeter

import examples.greeter.api.Greeter
import java.io.*
import javax.annotation.concurrent.Immutable;

@Immutable
class Friendly: Greeter {
    override fun hello(): String {
        val stream = javaClass.getResourceAsStream("/greeting.txt")
        val reader = BufferedReader(InputStreamReader(stream, "utf-8"))
        return reader.readLine()
    }
}
