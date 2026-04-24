import examples.greeter.api.Greeter;

module greeter.runner {
    requires greeter.api;

    requires org.apache.groovy;
    uses Greeter;
}
