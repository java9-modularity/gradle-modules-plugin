import java.io.FilenameFilter
import org.javamodularity.moduleplugin.tasks.ModularJavaExec
import org.javamodularity.moduleplugin.tasks.ModuleOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation(project(":greeter.api"))
    runtimeOnly(project(":greeter.provider"))
}

val moduleName: String by project

for(file in File("${project.projectDir}/src/main/kotlin/demo")
        .listFiles(FilenameFilter { _, name ->  name.matches(Regex("Demo.*\\.kt"))})) {
    val demoClassName = file.name.substring(0, file.name.length - ".kt".length)
    tasks.create<ModularJavaExec>("run$demoClassName") {
        group = "Demo"
        description = "Run the $demoClassName program"
        main = "$moduleName/demo.${demoClassName}Kt"
        jvmArgs = listOf("-Xmx128m")
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
