modularity.standardJavaRelease(11)

dependencies {
    runtimeOnly(project(":greeter.provider-jdk11"))
}

apply(from = "$rootDir/gradle/shared/greeter.provider.test.gradle")
