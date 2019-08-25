package tests

import examples.greeter.api.Greeter
import groovy.transform.CompileStatic

import java.util.ServiceLoader

@CompileStatic
class GreeterLocator {
    Greeter findGreeter() {
        ServiceLoader.load(Greeter).findFirst().orElseThrow { new RuntimeException('No Greeter found') }
    }
}
