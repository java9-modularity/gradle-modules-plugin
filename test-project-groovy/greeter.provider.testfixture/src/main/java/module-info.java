import examples.greeter.api.Greeter;

module greeter.provider.testfixture {
    requires greeter.api;
    requires org.apache.groovy;

    exports testfixture;
    uses Greeter;
}