import examples.greeter.api.Greeter;

module greeter.javaexec {
    requires greeter.api;
    requires org.codehaus.groovy;

    exports demo;
    uses Greeter;
}
