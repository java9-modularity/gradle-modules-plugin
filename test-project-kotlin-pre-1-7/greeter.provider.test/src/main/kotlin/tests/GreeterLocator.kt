package tests

import examples.greeter.api.Greeter

import java.util.ServiceLoader

class GreeterLocator {
    fun findGreeter(): Greeter {
        return ServiceLoader.load(Greeter::class.java)
                .findFirst()
                .orElseThrow{RuntimeException("No Greeter found")}
    }
}
