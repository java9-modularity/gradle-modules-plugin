package examples;

import examples.greeter.api.Greeter;

import java.util.Arrays;
import java.util.ServiceLoader;

public class Runner {
    public static void main(String[] args) {
        System.out.println("args: " + Arrays.asList(args));
        System.out.println("greeter.sender: " + System.getProperty("greeter.sender"));
        Greeter greeter = ServiceLoader.load(Greeter.class).findFirst().orElseThrow(() -> new RuntimeException("No Greeter found!"));
        System.out.println(greeter.hello());

        var resource = Runner.class.getResourceAsStream("/resourcetest.txt");
        if(resource == null) {
            throw new RuntimeException("Couldn't load resource");
        }

        ModuleLayer.boot().modules().stream()
                .map(Module::getName)
                .filter(m -> m.equals("java.sql"))
                .findAny().orElseThrow(() -> new RuntimeException("Expected module java.sql not found"));
    }
}
