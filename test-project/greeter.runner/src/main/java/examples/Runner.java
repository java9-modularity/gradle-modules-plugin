package examples;

import examples.greeter.api.Greeter;

import java.util.ServiceLoader;

public class Runner {
    public static void main(String[] args) {
        System.out.println("######## zzz ####### ARGS:");
        for (String arg: args) {
            System.out.println("######## zzz ####### " + arg);
        }

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
