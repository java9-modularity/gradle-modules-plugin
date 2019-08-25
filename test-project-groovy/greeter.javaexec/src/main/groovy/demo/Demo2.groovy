package demo

import examples.greeter.api.Greeter

public class Demo2 {
    static void main(String[] args) {
        Greeter greeter = ServiceLoader.load(Greeter).findFirst().orElseThrow {
            new RuntimeException("No Greeter found!")
        }
        println "Demo2: " + greeter.hello()
    }
}
