import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0" apply false
    id("org.javamodularity.moduleplugin") version "1.8.15" apply false
}

if (gradle.gradleVersion >= "8.0") {
    throw GradleException("The Kotlin version used in this build isn't compatible with Gradle 8. " +
            "This project should be excluded when testing with Gradle version 8 and above.")
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.javamodularity.moduleplugin")

    //region https://docs.gradle.org/current/userguide/kotlin_dsl.html#using_kotlin_delegated_properties
    val test by tasks.existing(Test::class)
    val build by tasks
    val javadoc by tasks

    val implementation by configurations
    val testImplementation by configurations
    val testRuntimeOnly by configurations

    val jUnitVersion: String by project
    val jUnitPlatformVersion: String by project
    //endregion

    //region KOTLIN
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    dependencies {
        implementation(kotlin("stdlib-jdk8"))
    }
    //endregion

    repositories {
        mavenCentral()
    }

    configure<org.javamodularity.moduleplugin.extensions.ModularityExtension> {
        improveEclipseClasspathFile()
        moduleVersion("1.2.3")
    }

    test {
        useJUnitPlatform()

        testLogging {
            events("PASSED", "FAILED", "SKIPPED", "STANDARD_OUT")
        }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$jUnitVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:$jUnitPlatformVersion")
    }

//    build.dependsOn(javadoc) // TODO: No public or protected classes found to document
}
