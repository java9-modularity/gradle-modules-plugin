import examples.greeter.api.Greeter;

module greeter.javaexec {
    requires greeter.api;
    requires kotlin.stdlib;

    uses Greeter;
}
