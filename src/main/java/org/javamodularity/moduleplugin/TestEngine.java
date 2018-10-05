package org.javamodularity.moduleplugin;

import java.util.Arrays;
import java.util.Optional;

public enum TestEngine {
    JUNIT_4("junit", "junit", "junit", "JUnit 4", "junit"),
    JUNIT_5("org.junit.jupiter", "junit-jupiter-api", "org.junit.jupiter.api", "JUnit 5", "org.junit.platform.commons"),
    TESTNG("org.testng", "testng", "testng", "TestNG", "testng");

    private final String groupId;
    private final String artifactId;
    private final String moduleName;
    private final String name;
    private final String addOpens;

    TestEngine(String groupId, String artifactId, String moduleName, String name, String addOpens) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.moduleName = moduleName;
        this.name = name;
        this.addOpens = addOpens;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getName() {
        return name;
    }

    public String getAddOpens() {
        return addOpens;
    }

    public static Optional<TestEngine> select(String groupId, String artifactId) {
        return Arrays.stream(TestEngine.values()).filter(engine -> engine.groupId.equals(groupId) && engine.artifactId.equals(artifactId)).findAny();
    }
}
