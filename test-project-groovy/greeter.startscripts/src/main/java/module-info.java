import examples.greeter.api.Greeter;

module greeter.startscripts {
    requires greeter.api;
    requires org.apache.groovy;

    exports startscripts;
    uses Greeter;
}
