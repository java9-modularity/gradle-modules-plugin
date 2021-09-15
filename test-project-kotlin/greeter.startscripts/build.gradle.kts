import org.javamodularity.moduleplugin.tasks.ModularCreateStartScripts
import org.javamodularity.moduleplugin.tasks.ModularJavaExec

plugins {
    application
}

//region https://docs.gradle.org/current/userguide/kotlin_dsl.html#using_kotlin_delegated_properties
val moduleName: String by project
val installDist by tasks
//endregion

dependencies {
    implementation(project(":greeter.api"))
    runtimeOnly(project(":greeter.provider"))
}

modularity {
    patchModule("java.annotation", "jsr250-api-1.0.jar")
    patchModule("java.annotation", "jsr305-3.0.2.jar")
}

application {
    mainClass.set("startscripts.MainDemoKt")
    mainModule.set(moduleName)
    applicationName = "demo"
    applicationDefaultJvmArgs = listOf("-Xmx128m")
}

File("${project.projectDir}/src/main/kotlin/startscripts")
        .listFiles({ _, name -> Regex("Demo.*\\.kt") matches name })
        .forEach { file ->
            val demoClassName = file.name.removeSuffix(".kt")

            val runDemo = tasks.create<ModularJavaExec>("run$demoClassName") {
                group = "Demo"
                description = "Run the $demoClassName program"
                main = "$moduleName/startscripts.${demoClassName}Kt"
                jvmArgs = listOf("-Xmx128m")
            }

            val createScripts = tasks.create<ModularCreateStartScripts>("createStartScripts$demoClassName") {
                runTask = runDemo
                applicationName = demoClassName.decapitalize()
            }

            installDist.finalizedBy(createScripts)
        }
