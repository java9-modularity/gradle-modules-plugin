package startscripts;

import examples.greeter.api.Greeter;
import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Test docs
 */
public class DemoHelper {
    /**
     * Test docs
     * @param appName test docs
     * @param args test docs
     */
    public static void greet(String appName, String[] args) {
        Greeter greeter = ServiceLoader.load(Greeter.class).findFirst().orElseThrow(() -> new RuntimeException("No Greeter found!"));
        String addition = System.getProperty("greeting.addition", "");
        if(!addition.isBlank()) addition = " " + addition;
        System.out.println(appName + ": " + greeter.hello() + addition + Arrays.stream(args).collect(Collectors.joining(" and ", ", ", "!")));
    }
}
