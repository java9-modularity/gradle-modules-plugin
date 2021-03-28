package testfixture

import examples.greeter.api.Greeter
import groovy.transform.CompileStatic

@CompileStatic
class GreeterLocator {
    Greeter findGreeter() {
        ServiceLoader.load(Greeter).findFirst().orElseThrow { new RuntimeException('No Greeter found') }
    }
}
