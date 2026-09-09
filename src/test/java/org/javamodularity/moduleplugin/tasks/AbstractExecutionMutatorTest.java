package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractExecutionMutatorTest {

    private static final String MODULE = "greeter.startscripts";
    private static final String MAIN_CLASS = "startscripts.Demo1";

    @Test
    void mainClassNameIsQualifiedWithTheMainModule() {
        JavaExec execTask = execTask(MAIN_CLASS);

        assertEquals(MODULE + "/" + MAIN_CLASS, mutatorFor(execTask).getMainClassName());
    }

    @Test
    void alreadyQualifiedMainClassIsNotQualifiedTwice() {
        JavaExec execTask = execTask(MODULE + "/" + MAIN_CLASS);

        assertEquals(MODULE + "/" + MAIN_CLASS, mutatorFor(execTask).getMainClassName(),
                "mainClass can arrive already module-qualified: ModularJavaExec.setMain() strips the "
                        + "prefix, but it is not always the setter that runs. Compiled against Gradle 9, "
                        + "where JavaExecSpec.setMain(String) no longer exists, javac emits no covariant "
                        + "bridge for the override, so on Gradle 8.x a Groovy DSL `main = \"module/Class\"` "
                        + "assignment reaches Gradle's own setMain instead. Qualifying twice yields "
                        + "`--module module/module/Class`, and the JVM then fails with "
                        + "\"Could not find or load main class module/Class in module module\".");
    }

    @Test
    void setMainStripsTheModulePrefix() {
        ModularJavaExec execTask = ProjectBuilder.builder()
                .withProjectDir(new File("test-project/greeter.startscripts/")).build()
                .getTasks().create("run", ModularJavaExec.class);

        execTask.setMain(MODULE + "/" + MAIN_CLASS);

        assertEquals(MAIN_CLASS, execTask.getMain(),
                "setMain() stores the bare class name so that the module is applied exactly once, "
                        + "by getMainClassName()");
    }

    private static JavaExec execTask(String mainClass) {
        Project project = ProjectBuilder.builder()
                .withProjectDir(new File("test-project/greeter.startscripts/")).build();
        project.getPlugins().apply("java");

        JavaExec execTask = project.getTasks().create("run", JavaExec.class);
        execTask.getMainClass().set(mainClass);
        execTask.getMainModule().set(MODULE);
        return execTask;
    }

    private static AbstractExecutionMutator mutatorFor(JavaExec execTask) {
        return new AbstractExecutionMutator(execTask, execTask.getProject()) {
        };
    }
}
