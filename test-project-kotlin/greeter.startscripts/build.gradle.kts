import java.io.FilenameFilter
import org.javamodularity.moduleplugin.tasks.ModularJavaExec
import org.javamodularity.moduleplugin.tasks.ModularCreateStartScripts
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
    mainClassName = "$moduleName/startscripts.MainDemoKt"
    applicationName = "demo"
    applicationDefaultJvmArgs = listOf("-Xmx128m")
}

for(file in File("${project.projectDir}/src/main/kotlin/startscripts")
        .listFiles(FilenameFilter { _, name ->  name.matches(Regex("Demo.*\\.kt"))})) {
    val demoClassName = file.name.substring(0, file.name.length - ".kt".length)
    tasks.create<ModularJavaExec>("run$demoClassName") {
        group = "Demo"
        description = "Run the $demoClassName program"
        main = "$moduleName/startscripts.${demoClassName}Kt"
        jvmArgs = listOf("-Xmx128m")
    }


    tasks.create<ModularCreateStartScripts>("createStartScripts$demoClassName") {
        runTask = tasks.getByName("run$demoClassName") as ModularJavaExec
        applicationName = demoClassName.decapitalize()
    }

    tasks.getByName("installDist").finalizedBy(tasks.getByName("createStartScripts$demoClassName"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

patchModules.config = listOf("java.annotation=jsr305-3.0.2.jar")
