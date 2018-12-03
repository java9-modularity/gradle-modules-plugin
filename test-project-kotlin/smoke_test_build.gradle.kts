buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}
plugins {
    val kotlinVersion = "1.3.10"
    kotlin("jvm") version kotlinVersion
    id("org.javamodularity.moduleplugin")
}

repositories {
    mavenCentral()
}

subprojects {
    repositories {
        mavenCentral()
    }
    apply {
        plugin("kotlin")
        plugin("org.javamodularity.moduleplugin")
    }
    project.version = "1.1.1"

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    val test by tasks.getting(Test::class) {
        useJUnitPlatform()

        testLogging {
            events("PASSED", "FAILED", "SKIPPED")
        }
    }

    val implementation by configurations
    val testImplementation by configurations
    val testRuntimeOnly by configurations
    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.3.1")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
    }
}
