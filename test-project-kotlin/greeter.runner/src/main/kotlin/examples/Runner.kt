package examples

import examples.greeter.api.Greeter

import java.util.ServiceLoader

fun main(args: Array<String>) {
    println("args: " + java.util.Arrays.asList(*args))
    println("greeter.sender: " + System.getProperty("greeter.sender"))
    val greeter = ServiceLoader.load(Greeter::class.java)
            .findFirst()
            .orElseThrow{RuntimeException("No Greeter found!")}
    println(greeter.hello())

    val resource = object: Any() {}.javaClass.getResourceAsStream("/resourcetest.txt")
    if(resource == null) {
        throw RuntimeException("Couldn't load resource")
    }
    ModuleLayer.boot().modules().map(Module::getName)
            .find{it == "java.sql"} ?: throw RuntimeException("Expected module java.sql not found")
}
