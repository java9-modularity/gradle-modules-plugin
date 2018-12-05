package demo;

import examples.greeter.api.Greeter;
import java.util.ServiceLoader;

public class Demo2 {
    public static void main(String[] args) {
        Greeter greeter = ServiceLoader.load(Greeter.class).findFirst().orElseThrow(() -> new RuntimeException("No Greeter found!"));
        System.out.println("Demo2: " + greeter.hello());
    }
}
