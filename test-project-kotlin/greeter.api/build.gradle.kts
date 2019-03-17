import org.javamodularity.moduleplugin.tasks.ModuleOptions
import org.javamodularity.moduleplugin.tasks.TestModuleOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//region NO-OP (DSL testing)
tasks {
    compileJava {
        extensions.configure(ModuleOptions::class) {
            addModules = listOf()
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
