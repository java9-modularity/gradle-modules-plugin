package demo;

import examples.greeter.api.Greeter;
import java.util.ServiceLoader;

/**
 * Test docs
 */
public class Demo1 {
    /**
     * Test docs
     * @param args test docs
     */
    public static void main(String[] args) {
        Greeter greeter = ServiceLoader.load(Greeter.class).findFirst().orElseThrow(() -> new RuntimeException("No Greeter found!"));
        System.out.println("Demo1: " + greeter.hello());
    }
}
