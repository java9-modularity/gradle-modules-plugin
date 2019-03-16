import org.javamodularity.moduleplugin.extensions.CompileModuleOptions
import org.javamodularity.moduleplugin.tasks.TestModuleOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//region NO-OP (DSL testing)
tasks {
    compileJava {
        extensions.configure(CompileModuleOptions::class) {
            addModules = listOf()
            compileModuleInfoSeparately = false
        }
    }

    test {
        extensions.configure(TestModuleOptions::class) {
            addModules = listOf()
            runOnClasspath = false
        }
    }
}
//endregion

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
