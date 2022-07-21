import examples.greeter.api.Greeter;

module greeter.startscripts {
    requires greeter.api;
    requires kotlin.stdlib;

    uses Greeter;
}
