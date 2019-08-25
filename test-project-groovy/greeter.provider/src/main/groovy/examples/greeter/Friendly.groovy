package examples.greeter

import examples.greeter.api.Greeter
import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.IOGroovyMethods

@CompileStatic
class Friendly implements Greeter {
    @Override
    String hello() {
        var stream = this.getClass().getResourceAsStream('/greeting.txt')
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, 'utf-8'))
        IOGroovyMethods.withCloseable(reader) {
            return reader.readLine()
        }
    }
}
