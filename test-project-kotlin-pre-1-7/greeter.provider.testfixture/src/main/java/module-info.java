import examples.greeter.api.Greeter;

module greeter.provider.testfixture {
    requires greeter.api;
    requires kotlin.stdlib;
    uses Greeter;
}
