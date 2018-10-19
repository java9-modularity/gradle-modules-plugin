package org.javamodularity.moduleplugin;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("ConstantConditions")
class ModulePluginSmokeTest {

    private List<File> pluginClasspath;

    @BeforeEach
    void before() throws IOException {
        pluginClasspath = Resources.readLines(Resources.getResource("plugin-classpath.txt"), Charsets.UTF_8)
                .stream()
                .map(fname -> new File(fname))
                .collect(Collectors.toList());
    }

    @Test
    void smokeTest() {
        var result = GradleRunner.create()
                .withProjectDir(new File("test-project"))
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion("4.10.2")
                .build();


        System.out.println("Build result");
        System.out.println("============");
        System.out.println(result.getOutput());
        System.out.println("============");
        assertEquals(TaskOutcome.SUCCESS, result.task(":greeter.api:build").getOutcome(), "Failed Build!");
        assertEquals(TaskOutcome.SUCCESS, result.task(":greeter.provider:build").getOutcome(), "Failed Build!");
        assertEquals(TaskOutcome.SUCCESS, result.task(":greeter.provider.test:build").getOutcome(), "Failed Build!");
        assertEquals(TaskOutcome.SUCCESS, result.task(":greeter.runner:build").getOutcome(), "Failed Build!");
    }
}
