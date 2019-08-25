package startscripts

import examples.greeter.api.Greeter

public class Demo1 {
    public static void main(String[] args) {
        Greeter greeter = ServiceLoader.load(Greeter).findFirst().orElseThrow {
            new RuntimeException("No Greeter found!")
        }
        println "Demo1: " + greeter.hello()
    }
}
