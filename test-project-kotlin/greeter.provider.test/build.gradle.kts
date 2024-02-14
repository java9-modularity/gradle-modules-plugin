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

val generatedResourcesDir = "generated/resources/test";

sourceSets {
    test {
        // Add additional output directory for generated resources.
        // See org.gradle.api.tasks.SourceSetOutput for more info.
        output.dir(layout.buildDirectory.dir(generatedResourcesDir))
    }
}

val generateResources = tasks.register("generateResources") {
    doLast {
        val outputFile = layout.buildDirectory.file("$generatedResourcesDir/generated-resource.txt")
        outputFile.get().asFile.parentFile.mkdirs()
        outputFile.get().asFile.writeText("some content")

        println("Resource file generated at: ${outputFile.get().asFile.absolutePath}")
    }
}

tasks.test {
    dependsOn(generateResources)
}
