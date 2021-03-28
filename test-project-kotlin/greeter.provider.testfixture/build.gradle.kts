plugins {
    `java-test-fixtures`
}

//// Include the main sourceset as a dependency of testFixtures.
//kotlin.target.compilations.getByName("testFixtures") {
//    associateWith(target.compilations.getByName("main"))
//}

dependencies {
    implementation(project(":greeter.api"))
    testFixturesImplementation(project(":greeter.api"))
    runtimeOnly(project(":greeter.provider"))
}

modularity {
    patchModule("java.annotation", "jsr305-3.0.2.jar")
    patchModule("java.annotation", "jsr250-api-1.0.jar")
}
