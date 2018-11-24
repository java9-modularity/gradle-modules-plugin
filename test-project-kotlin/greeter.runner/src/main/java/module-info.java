import examples.greeter.api.Greeter;

module greeter.runner {
    requires greeter.api;
    requires kotlin.stdlib;
    uses Greeter;
}
