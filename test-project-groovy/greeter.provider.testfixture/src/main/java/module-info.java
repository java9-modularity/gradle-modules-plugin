import examples.greeter.api.Greeter;

module greeter.provider.testfixture {
    requires greeter.api;
    requires org.codehaus.groovy;

    exports testfixture;
    uses Greeter;
}