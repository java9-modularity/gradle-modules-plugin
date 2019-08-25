package demo

import examples.greeter.api.Greeter

class Demo1 {
    static void main(String[] args) {
        Greeter greeter = ServiceLoader.load(Greeter).findFirst().orElseThrow {
            new RuntimeException("No Greeter found!")
        }
        println "Demo1: " + greeter.hello()
    }
}
