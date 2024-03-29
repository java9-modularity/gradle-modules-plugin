package examples.greeter;

import examples.greeter.api.Greeter;
import java.io.*;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Test docs
 */
public class Friendly implements Greeter {
    /**
     * Test docs
     */
    @Override @Nonnull
    public String hello() {
        InputStream stream = this.getClass().getResourceAsStream("/greeting.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf-8"))) {
            return reader.readLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
