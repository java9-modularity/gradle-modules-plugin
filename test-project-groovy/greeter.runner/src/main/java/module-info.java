import examples.greeter.api.Greeter;

module greeter.runner {
    requires greeter.api;

    requires org.codehaus.groovy;
    uses Greeter;
}
