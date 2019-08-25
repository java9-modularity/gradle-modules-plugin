import examples.greeter.api.Greeter;

module greeter.provider.test {
    requires greeter.api;
    requires org.codehaus.groovy;

    exports tests;
    uses Greeter;
}