import org.javamodularity.moduleplugin.extensions.RunModuleOptions

plugins {
    application
}

//region https://docs.gradle.org/current/userguide/kotlin_dsl.html#using_kotlin_delegated_properties
val moduleName: String by project
val run by tasks.existing(JavaExec::class) // https://youtrack.jetbrains.com/issue/KT-28013
//endregion

dependencies {
    implementation(project(":greeter.api"))
    runtimeOnly(project(":greeter.provider"))
}

application {
    mainClass.set("examples.RunnerKt")
    mainModule.set(moduleName)
    applicationDefaultJvmArgs = listOf("-Dgreeter.sender=gradle-modules-plugin")
}

modularity {
    patchModule("java.annotation", "jsr305-3.0.2.jar")
}
patchModules.config = listOf(
    "java.annotation=jsr250-api-1.0.jar"
)

(run) {
    extensions.configure<RunModuleOptions> {
        addModules = listOf("java.sql")
    }

    jvmArgs = listOf("-Dgreeter.sender=gradle-modules-plugin")
}
