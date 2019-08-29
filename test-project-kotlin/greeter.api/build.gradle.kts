import org.javamodularity.moduleplugin.extensions.CompileModuleOptions
import org.javamodularity.moduleplugin.extensions.TestModuleOptions

//region NO-OP (DSL testing)
tasks.compileJava {
    extensions.configure<CompileModuleOptions> {
        addModules = listOf()
        compileModuleInfoSeparately = false
    }
}

tasks.test {
    extensions.configure<TestModuleOptions> {
        addModules = listOf()
        runOnClasspath = false
    }
}

modularity {
}
//endregion
