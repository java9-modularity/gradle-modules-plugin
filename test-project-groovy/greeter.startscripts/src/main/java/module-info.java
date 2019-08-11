import examples.greeter.api.Greeter;

module greeter.startscripts {
    requires greeter.api;
    requires org.codehaus.groovy;

    exports startscripts;
    uses Greeter;
}
