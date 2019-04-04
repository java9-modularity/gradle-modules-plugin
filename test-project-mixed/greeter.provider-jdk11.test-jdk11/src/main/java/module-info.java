import examples.greeter.api.Greeter;

module greeter.provider.test {
    requires greeter.api;

    uses Greeter;
}