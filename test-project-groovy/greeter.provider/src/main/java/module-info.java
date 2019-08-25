import examples.greeter.api.Greeter;

module greeter.provider {
    requires greeter.api;
    requires org.codehaus.groovy;

    exports examples.greeter;
    provides Greeter with examples.greeter.Friendly;
}