package examples;

import examples.greeter.api.Greeter;

import java.util.ServiceLoader;

public class Runner {
    public static void main(String[] args) {
        Greeter greeter = ServiceLoader.load(Greeter.class).findFirst().orElseThrow(() -> new RuntimeException("No Greeter found!"));
        System.out.println(greeter.hello());
    }
}
