import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.20" apply false
    id("org.javamodularity.moduleplugin") version "1.5.0" apply false
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

    test {
        useJUnitPlatform()

        testLogging {
            events("PASSED", "FAILED", "SKIPPED")
            setStackTraceFilters(listOf())
        }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$jUnitVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
    }

//    build.dependsOn(javadoc) // TODO: No public or protected classes found to document
}
