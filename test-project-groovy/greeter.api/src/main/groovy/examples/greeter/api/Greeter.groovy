package examples.greeter.api

import groovy.transform.CompileStatic

@CompileStatic
interface Greeter {
    String hello()
}
