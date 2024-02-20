package tests;

import examples.greeter.api.Greeter;

import java.util.ServiceLoader;

/**
 * Test docs
 */
public class GreeterLocator {
    /**
     * Test docs
     * @return test docs
     */
    public Greeter findGreeter() {
        return ServiceLoader.load(Greeter.class).findFirst().orElseThrow(() -> new RuntimeException("No Greeter found"));
    }
}
