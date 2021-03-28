import examples.greeter.api.Greeter;

module greeter.provider.testfixture {
    requires greeter.api;

    uses Greeter;
}