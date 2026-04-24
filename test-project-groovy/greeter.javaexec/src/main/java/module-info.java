import examples.greeter.api.Greeter;

module greeter.javaexec {
    requires greeter.api;
    requires org.apache.groovy;

    exports demo;
    uses Greeter;
}
