package startscripts;

import examples.greeter.api.Greeter;
import java.util.ServiceLoader;

public class MainDemo {
    public static void main(String[] args) {
        Greeter greeter = ServiceLoader.load(Greeter.class).findFirst().orElseThrow(() -> new RuntimeException("No Greeter found!"));
        System.out.println("MainDemo: " + greeter.hello());
    }
}
