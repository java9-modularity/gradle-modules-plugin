import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation(project(":greeter.api"))
    testImplementation("org.hamcrest:hamcrest:2.1-rc4")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
