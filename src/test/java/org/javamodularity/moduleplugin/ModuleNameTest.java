package org.javamodularity.moduleplugin;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ModuleNameTest {

    @Test
    void findModuleName_ParentProjectWithNoModules() {
        Project project = ProjectBuilder.builder().withProjectDir(new File("test-project/")).build();
        project.getPlugins().apply("java");
        Optional<String> result = new ModuleName().findModuleName(project);

        assertTrue(result.isEmpty(), "Found a module in the parent project. It doesn't have any modules or java code so it should not find anything.");
    }

    @Test
    void findModuleName_ParentProjectWithWithJava() {
        Project project = ProjectBuilder.builder().withProjectDir(new File("test-project/")).build();
        project.getPlugins().apply("java");

        Optional<String> result = new ModuleName().findModuleName(project);

        assertTrue(result.isEmpty(), "Found a module in the parent project. It doesn't have any modules or java code so it should not find anything.");
    }

    @Test
    void findModuleName_ModularSubproject() {
        Project project = ProjectBuilder.builder().withProjectDir(new File("test-project/greeter.api/")).build();
        project.getPlugins().apply("java");

        Optional<String> result = new ModuleName().findModuleName(project);

        assertEquals("greeter.api", result.orElseThrow(), "Unexpected module name or the module is missing");
    }

}