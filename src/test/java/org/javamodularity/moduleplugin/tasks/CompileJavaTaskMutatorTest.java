package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompileJavaTaskMutatorTest {

    @Test
    void mutateJavaCompileTask() {
        // given
        Project project = ProjectBuilder.builder().withProjectDir(new File("test-project/")).build();
        project.getPlugins().apply("java");
        final JavaCompile compileJava = (JavaCompile) project.getTasks().findByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);

        // when
        CompileJavaTaskMutator.mutateJavaCompileTask(project, compileJava);

        // then
        List<String> twoLastArguments = twoLastCompilerArgs(compileJava);
        assertEquals(
                Arrays.asList("--module-path", compileJava.getClasspath().getAsPath()),
                twoLastArguments,
                "Two last arguments should be setting module path to the current compileJava task classpath");
    }

    private List<String> twoLastCompilerArgs(JavaCompile compileJava) {
        List<String> allCompilerArgs = compileJava.getOptions().getAllCompilerArgs();
        int size = allCompilerArgs.size();
        return allCompilerArgs.subList(size-2, size);
    }

}