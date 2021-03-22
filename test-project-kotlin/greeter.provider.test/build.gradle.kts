dependencies {
    implementation(project(":greeter.api"))
    runtimeOnly(project(":greeter.provider"))
}

modularity {
    patchModule("java.annotation", "jsr305-3.0.2.jar")
    patchModule("java.annotation", "jsr250-api-1.0.jar")
}

val compileTestJava: JavaCompile by tasks.named("compileTestJava")
val moduleOptions: org.javamodularity.moduleplugin.extensions.ModuleOptions by compileTestJava.extensions
moduleOptions.addModules = listOf("jdk.unsupported")
