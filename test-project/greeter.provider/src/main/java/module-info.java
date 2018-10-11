import examples.greeter.api.Greeter;

module greeter.provider {
    requires greeter.api;

    provides Greeter with examples.greeter.Friendly;
}