import examples.greeter.api.Greeter;

module greeter.demo {
    requires greeter.api;
    requires kotlin.stdlib;

    uses Greeter;
}
