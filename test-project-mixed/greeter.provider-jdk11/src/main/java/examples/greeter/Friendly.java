package examples.greeter;

import examples.greeter.api.Greeter;
import java.io.*;
import java.util.*;
import javax.annotation.Nonnull;

public class Friendly implements Greeter {
    @Override @Nonnull
    public String hello() {
        var stream = this.getClass().getResourceAsStream("/greeting.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf-8"))) {
            return reader.readLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
