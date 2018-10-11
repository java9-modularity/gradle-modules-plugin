package org.javamodularity.moduleplugin;

import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("ConstantConditions")
class ModulePluginSmokeTest {

    @Test
    void smokeTest() {
        var result = GradleRunner.create()
                .withProjectDir(new File("test-project"))
                .withPluginClasspath()
                .withGradleVersion("4.10.2")
                .withArguments("clean", "build")
                .build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":build").getOutcome(),
                "Failed Build: "+ result.getOutput());
    }
}
