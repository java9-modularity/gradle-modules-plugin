package testfixture

import examples.greeter.api.Greeter
import spock.lang.Specification

class GreeterSpecification extends Specification {
    def "test locate"() {
        given:
        Greeter greeter = new GreeterLocator().findGreeter()
        expect:
        !greeter.hello().blank
    }
}
