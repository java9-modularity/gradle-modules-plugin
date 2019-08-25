package startscripts

import examples.greeter.api.Greeter

public class MainDemo {
    public static void main(String[] args) {
        Greeter greeter = ServiceLoader.load(Greeter).findFirst().orElseThrow {
            new RuntimeException("No Greeter found!")
        }
        println "MainDemo: " + greeter.hello()
    }
}
