package org.javamodularity.moduleplugin;

import org.gradle.api.Project;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public enum TestEngine {

    JUNIT_4("junit", "junit", "junit", "junit"),
    JUNIT_5("org.junit.jupiter", "junit-jupiter-api", "org.junit.jupiter.api", "org.junit.platform.commons"),
    TESTNG("org.testng", "testng", "testng", "testng");

    private final String groupId;
    private final String artifactId;

    public final String moduleName;
    public final String addOpens;

    TestEngine(String groupId, String artifactId, String moduleName, String addOpens) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.moduleName = moduleName;
        this.addOpens = addOpens;
    }

    public static Optional<TestEngine> select(Project project) {
        var configurations = project.getConfigurations();
        var testImplementation = configurations.getByName("testImplementation").getDependencies().stream();
        var testCompile = configurations.getByName("testCompile").getDependencies().stream();
        return Stream.concat(testImplementation, testCompile)
                .map(d -> TestEngine.select(d.getGroup(), d.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }

    private static Optional<TestEngine> select(String groupId, String artifactId) {
        return Arrays.stream(TestEngine.values()).filter(engine -> engine.groupId.equals(groupId) && engine.artifactId.equals(artifactId)).findAny();
    }

}
