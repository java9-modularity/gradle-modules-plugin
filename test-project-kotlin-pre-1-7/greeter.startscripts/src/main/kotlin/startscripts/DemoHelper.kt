package startscripts

import examples.greeter.api.Greeter
import java.util.ServiceLoader

object DemoHelper {
    fun greet(appName: String, args: Array<String>) {
        val greeter = ServiceLoader.load(Greeter::class.java)
                .findFirst()
                .orElseThrow{RuntimeException("No Greeter found!")}
        val addition = System.getProperty("greeting.addition", "").let {
            if(it.isBlank()) "" else " $it"
        }
        println("$appName: ${greeter.hello()}$addition" + args.joinToString(" and ", ", ", "!"))
    }
}
