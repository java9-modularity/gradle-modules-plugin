import examples.greeter.api.Greeter;

module greeter.provider {
    requires greeter.api;
    requires kotlin.stdlib;
    requires java.annotation;
    provides Greeter with examples.greeter.Friendly;
}
