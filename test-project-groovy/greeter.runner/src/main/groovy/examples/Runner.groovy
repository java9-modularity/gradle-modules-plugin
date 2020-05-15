package examples

import examples.greeter.api.Greeter
import groovy.transform.CompileStatic

import java.util.ServiceLoader

@CompileStatic
class Runner {
    static void main(String[] args) {
        println "args: $args"
        println "greeter.sender: ${System.properties['greeter.sender']}"
        Greeter greeter = ServiceLoader.load(Greeter.class).findFirst().orElseThrow { new RuntimeException('No Greeter found!') }
        println(greeter.hello())
        if(Version.value != '3.14.15') {
            throw new RuntimeException("Invalid version: $Version.value")
        }

        def resource = Runner.class.getResourceAsStream('/resourcetest.txt')
        if(!resource) {
            throw new RuntimeException("Couldn't load resource")
        }

        def moduleNames = ModuleLayer.boot().modules().collect { it.name }
        if(!moduleNames.contains('java.sql')) {
            throw new RuntimeException('Expected module java.sql not found')
        }
    }
}
