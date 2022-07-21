import org.javamodularity.moduleplugin.extensions.JavadocModuleOptions

dependencies {
    implementation(project(":greeter.api"))
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("javax.annotation:jsr250-api:1.0")

    testImplementation("org.hamcrest:hamcrest:2.1+")
}

patchModules.config = listOf(
        "java.annotation=jsr305-3.0.2.jar"
)

modularity {
    patchModule("java.annotation", "jsr250-api-1.0.jar")
}

tasks.javadoc {
    extensions.configure<JavadocModuleOptions> {
        addModules = listOf("java.sql")
    }
}
