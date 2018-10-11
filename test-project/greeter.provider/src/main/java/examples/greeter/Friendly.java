package examples.greeter;

import examples.greeter.api.Greeter;

public class Friendly implements Greeter {
    @Override
    public String hello() {
        return "Hello and welcome!";
    }
}
