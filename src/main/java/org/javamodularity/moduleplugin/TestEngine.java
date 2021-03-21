package org.javamodularity.moduleplugin;

import org.gradle.api.Project;
import org.gradle.api.artifacts.*;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.javamodularity.moduleplugin.internal.TaskOption;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TestEngine {
    JUNIT_4("junit", "junit", "junit", "junit"),
    JUNIT_5_API("org.junit.jupiter", ".*", "org.junit.jupiter.api", "org.junit.platform.commons"),
    JUNIT_5("org.junit.jupiter", ".*", "org.junit.jupiter.api", "org.junit.jupiter.api"),
    JUNIT_PLATFORM_COMMONS("org.junit.jupiter", ".*", "org.junit.platform.commons", "ALL-UNNAMED",
            new TaskOption("--add-exports", "org.junit.platform.commons/org.junit.platform.commons.util=ALL-UNNAMED"),
            new TaskOption("--add-exports", "org.junit.platform.commons/org.junit.platform.commons.logging=ALL-UNNAMED"),
            new TaskOption("--add-exports", "org.junit.platform.engine/org.junit.platform.engine.support.filter=org.junit.jupiter.engine,ALL-UNNAMED")
    ),
    TESTNG("org.testng", "testng", "testng", "testng"),
    ASSERTJ("org.assertj", "assertj-core", "org.assertj.core", "org.assertj.core"),
    MOCKITO("org.mockito", "mockito-core", "org.mockito", "org.mockito"),
    EASYMOCK("org.easymock", "easymock", "org.easymock", "org.easymock"),
    SPOCK("org.spockframework", "spock-core", "org.spockframework.core", "org.spockframework.core",
            new TaskOption("--add-exports", "org.junit.platform.commons/org.junit.platform.commons.util=org.spockframework.core,ALL-UNNAMED"),
            new TaskOption("--add-exports", "org.junit.platform.commons/org.junit.platform.commons.logging=org.spockframework.core,ALL-UNNAMED"),
            new TaskOption("--add-exports", "org.junit.platform.engine/org.junit.platform.engine.support.filter=org.junit.jupiter.engine,ALL-UNNAMED")
    );

    private static final Logger LOGGER = Logging.getLogger(TestEngine.class);

    private final String groupId;
    private final String artifactId;

    public final String moduleName;
    public final String addOpens;
    public final List<TaskOption> additionalTaskOptions;

    TestEngine(String groupId, String artifactId, String moduleName, String addOpens, TaskOption... additionalTaskOptions) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.moduleName = moduleName;
        this.addOpens = addOpens;
        this.additionalTaskOptions = Arrays.asList(additionalTaskOptions);
    }

    public static Collection<TestEngine> selectMultiple(Project project) {
        var configurations = project.getConfigurations();
        var engines = List.of(
                "testImplementation", "testCompile", "testRuntime", "testRuntimeOnly",
                "testFixturesApi", "testFixturesCompile", "testFixturesImplementation", "testFixturesRuntime", "testFixturesRuntimeOnly")
                .stream()
                .map(configurations::findByName)
                .filter(Objects::nonNull)
                .map(Configuration::getDependencies)
                .flatMap(DependencySet::stream)
                .flatMap(d -> TestEngine.select(d.getGroup(), d.getName()))
                .collect(Collectors.toSet());
        LOGGER.info("Selected test engines: " + engines);
        return engines;

    }

    private static Stream<TestEngine> select(String groupId, String artifactId) {
        return Arrays.stream(TestEngine.values())
                .filter(engine -> groupId.matches(engine.groupId) && artifactId.matches(engine.artifactId));
    }
}
