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

        assertEquals(MODULE + "/" + MAIN_CLASS, mutatorFor(execTask).getMainClassName());
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
