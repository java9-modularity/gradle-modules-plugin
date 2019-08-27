package startscripts

import examples.greeter.api.Greeter

class DemoHelper {
    static void greet(String appName, String[] args) {
        Greeter greeter = ServiceLoader.load(Greeter).findFirst().orElseThrow {
            new RuntimeException("No Greeter found!")
        }
        String addition = System.properties['greeting.addition'] ?: ''
        if(!addition.blank) addition = " $addition"
        def recipients = args ? ", ${args.join(' and ')}!" : ''
        println "$appName: ${greeter.hello()}$addition$recipients"
    }
}
