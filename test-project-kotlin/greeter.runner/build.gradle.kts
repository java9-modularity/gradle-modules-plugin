import org.gradle.api.tasks.JavaExec
import org.javamodularity.moduleplugin.tasks.ModuleOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
}

dependencies {
    implementation(project(":greeter.api"))
    runtimeOnly(project(":greeter.provider"))
}

val moduleName: String by project
application {
    mainClassName = "$moduleName/examples.RunnerKt"
    applicationDefaultJvmArgs = listOf("-XX:+PrintGCDetails")
}

val run by tasks.getting(JavaExec::class) {
    extensions.configure(typeOf<ModuleOptions>()) {
        addModules = listOf("java.sql")
    }
    jvmArgs = listOf("-XX:+PrintGCDetails")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}