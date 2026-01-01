import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0" apply false
    id("org.javamodularity.moduleplugin") version "2.0.0" apply false
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.javamodularity.moduleplugin")

    //region https://docs.gradle.org/current/userguide/kotlin_dsl.html#using_kotlin_delegated_properties
    val test by tasks.existing(Test::class)
    val compileTestJava by tasks.existing(JavaCompile::class)

    val implementation by configurations
    val testImplementation by configurations
    val testRuntimeOnly by configurations

    val jUnitVersion: String by project
    val jUnitPlatformVersion: String by project
    //endregion

    //region KOTLIN
    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
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

        extensions.configure(org.javamodularity.moduleplugin.extensions.TestModuleOptions::class) {
            runOnClasspath = false
        }
    }

    compileTestJava {
        extensions.configure(org.javamodularity.moduleplugin.extensions.CompileTestModuleOptions::class) {
            compileOnClasspath = false
        }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$jUnitVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:$jUnitPlatformVersion")
    }
}
