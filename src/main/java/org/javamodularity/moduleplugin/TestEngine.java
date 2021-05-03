package org.javamodularity.moduleplugin;

import org.gradle.api.Project;
import org.gradle.api.artifacts.*;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.javamodularity.moduleplugin.internal.TaskOption;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TestEngine {
    JUNIT_4("junit", "junit", "junit", "junit"),
    JUNIT_5_API("org.junit.jupiter", ".*", "org.junit.jupiter.api", "org.junit.platform.commons"),
    JUNIT_5("org.junit.jupiter", "junit-platform-commons", "org.junit.jupiter.api", "org.junit.jupiter.api"),
    JUNIT_5_PARAMS("org.junit.jupiter", "junit-jupiter-params", "org.junit.jupiter.params", "org.junit.platform.commons"),
    JUNIT_PLATFORM_COMMONS("org.junit.platform", "junit-platform-commons", "org.junit.platform.commons", "ALL-UNNAMED",
            new TaskOption("--add-exports", "org.junit.platform.commons/org.junit.platform.commons.util=ALL-UNNAMED"),
            new TaskOption("--add-exports", "org.junit.platform.commons/org.junit.platform.commons.logging=ALL-UNNAMED")
    ),
    JUNIT_JUPITER_ENGINE("org.junit.jupiter", "junit-jupiter-engine", "org.junit.jupiter.engine", "ALL-UNNAMED"),
    JUNIT_PLATFORM_ENGINE("org.junit.platform", "junit-platform-engine", "org.junit.platform.engine", "ALL-UNNAMED",
            new TaskOption("--add-exports", "org.junit.platform.engine/org.junit.platform.engine.support.filter=org.junit.jupiter.engine,ALL-UNNAMED")
    ),
    TESTNG("org.testng", "testng", "org.testng", "org.testng"),
    ASSERTJ("org.assertj", "assertj-core", "org.assertj.core", "org.assertj.core"),
    TRUTH("com.google.truth", "truth", "truth", "truth"),
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

    private static class GroupArtifact {
        final String groupId;
        final String artifactId;

        private GroupArtifact(String groupId, String artifactId) {
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        static GroupArtifact fromModuleIdentifier(ModuleIdentifier mi) {
            LOGGER.info("fromModuleIdentifier({}:{})", mi.getGroup(), mi.getName());
            return new GroupArtifact(mi.getGroup(), mi.getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupArtifact that = (GroupArtifact) o;
            return Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, artifactId);
        }
    }

    TestEngine(String groupId, String artifactId, String moduleName, String addOpens, TaskOption... additionalTaskOptions) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.moduleName = moduleName;
        this.addOpens = addOpens;
        this.additionalTaskOptions = Arrays.asList(additionalTaskOptions);
    }

    private static List<String> CONFIGURATION_NAMES = List.of(
            "api", "compile", "compileClasspath", "compileOnly", "implementation",
            "runtime", "runtimeClasspath", "runtimeOnly",
            "testApi", "testCompile", "testCompileClasspath", "testCompileOnly",
            "testImplementation", "testRuntime", "testRuntimeClasspath", "testRuntimeOnly",
            "testFixturesApi", "testFixturesCompile", "testFixturesCompileClasspath", "testFixturesCompileOnly",
            "testFixturesCompileOnlyApi", "testFixturesImplementation", "testFixturesRuntime",
            "testFixturesRuntimeClasspath", "testFixturesRuntimeOnly"
    );

    public static Collection<TestEngine> selectMultiple(Project project, Set<File> files) {
        var configurations = project.getConfigurations();
        var engines = CONFIGURATION_NAMES
                .stream()
                .map(configurations::findByName)
                .filter(Objects::nonNull)
                .flatMap(cfg -> getModuleIdentifiers(cfg, files))
                .flatMap(d -> TestEngine.select(d))
                .collect(Collectors.toSet());
        LOGGER.info("Selected test engines: " + engines);
        return engines;

    }

    private static Stream<GroupArtifact> getModuleIdentifiers(Configuration origCfg, Set<File> files) {
        Configuration cfg = origCfg.copyRecursive();
        cfg.setCanBeResolved(true);
        try {
            cfg.resolve();
            Set<ResolvedDependency> flmDeps = cfg.getResolvedConfiguration().getFirstLevelModuleDependencies();
            return flmDeps.stream()
                    .flatMap(dep -> Stream.concat(getAllDeps(dep).stream(),Stream.of(dep)))
                    .filter(dep -> isDependencyPresent(dep, files))
                    .map(dep -> GroupArtifact.fromModuleIdentifier(dep.getModule().getId().getModule()));
        } catch (ResolveException e) {
            LOGGER.debug("Cannot resolve transitive dependencies of configuration " + cfg.getName(), e);
            LOGGER.info("Using direct dependencies of configuration {}.", origCfg.getName());
            return origCfg.getDependencies().stream()
                    .map(dep -> new GroupArtifact(dep.getGroup(), dep.getName()));
        }
    }

    private static boolean isDependencyPresent(ResolvedDependency dep, Set<File> files) {
        return dep.getModuleArtifacts().stream()
                .map(ResolvedArtifact::getFile)
                .anyMatch(files::contains);
    }

    private static Set<ResolvedDependency> getAllDeps(ResolvedDependency dep) {
        Set<ResolvedDependency> deps = new HashSet<>();
        addChildren(dep, deps);
        return deps;
    }

    private static void addChildren(ResolvedDependency dep, Set<ResolvedDependency> deps) {
        deps.add(dep);
        dep.getChildren().forEach(d -> {if(!deps.contains(d)) addChildren(d, deps);});
    }

    private static Stream<TestEngine> select(GroupArtifact ga) {
        LOGGER.info("TestEngine.select({}:{}", ga.groupId, ga.artifactId);
        return Arrays.stream(TestEngine.values())
                .filter(engine ->
                        ga.groupId != null
                        && ga.artifactId != null
                        && ga.groupId.matches(engine.groupId)
                        && ga.artifactId.matches(engine.artifactId));
    }
}
