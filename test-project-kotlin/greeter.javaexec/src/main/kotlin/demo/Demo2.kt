package demo

import examples.greeter.api.Greeter

import java.util.ServiceLoader

fun main(args: Array<String>) {
    val greeter = ServiceLoader.load(Greeter::class.java)
            .findFirst()
            .orElseThrow { RuntimeException("No Greeter found!") }
    println("Demo2: " + greeter.hello())
}
