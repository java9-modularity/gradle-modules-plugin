import examples.greeter.api.Greeter;

/**
 * Test docs
 */
module greeter.provider {
    requires greeter.api;
    requires java.annotation;

    provides Greeter with examples.greeter.Friendly;
}