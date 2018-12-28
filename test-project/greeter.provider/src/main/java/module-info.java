import examples.greeter.api.Greeter;

module greeter.provider {
    requires greeter.api;
    requires java.annotation;

    provides Greeter with examples.greeter.Friendly;
}