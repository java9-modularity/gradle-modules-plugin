package tests;

import examples.greeter.api.Greeter;

import java.util.ServiceLoader;

public class GreeterLocator {
    public Greeter findGreeter() {
        return ServiceLoader.load(Greeter.class).iterator().next(); // no ServiceLoader.findFirst() in JDK 8
    }
}
