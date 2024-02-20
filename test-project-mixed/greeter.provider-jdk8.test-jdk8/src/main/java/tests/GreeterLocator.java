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
        return ServiceLoader.load(Greeter.class).iterator().next(); // no ServiceLoader.findFirst() in JDK 8
    }
}
