import org.javamodularity.moduleplugin.tasks.ModularJavaExec

val moduleName: String by project

dependencies {
    implementation(project(":greeter.api"))
    runtimeOnly(project(":greeter.provider"))
}

patchModules.config = listOf(
        "java.annotation=jsr305-3.0.2.jar"
)

File("${project.projectDir}/src/main/kotlin/demo")
        .listFiles({ _, name -> Regex("Demo.*\\.kt") matches name })
        .forEach { file ->
            val demoClassName = file.name.removeSuffix(".kt")
            tasks.create<ModularJavaExec>("run$demoClassName") {
                group = "Demo"
                description = "Run the $demoClassName program"
                main = "$moduleName/demo.${demoClassName}Kt"
                jvmArgs = listOf("-Xmx128m")
            }
        }
