package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.testfixtures.ProjectBuilder;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;
import org.javamodularity.moduleplugin.extensions.DefaultModularityExtension;
import org.javamodularity.moduleplugin.extensions.PatchModuleExtension;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompileJavaTaskMutatorTest {

    @Test
    void modularizeJavaCompileTask() {
        // given
        Project project = ProjectBuilder.builder().withProjectDir(new File("test-project/")).build();
        project.getPlugins().apply("java");
        JavaCompile compileJava = (JavaCompile) project.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);

        FileCollection classpath = project.files("."); // we need anything on classpath
        compileJava.setClasspath(classpath);

        CompileModuleOptions moduleOptions = compileJava.getExtensions()
                .create("moduleOptions", CompileModuleOptions.class, project);
        project.getExtensions().add("moduleName", getClass().getName());
        project.getExtensions().create("patchModules", PatchModuleExtension.class);
        project.getExtensions().create("modularity", DefaultModularityExtension.class, project);

        CompileJavaTaskMutator mutator = new CompileJavaTaskMutator(project, compileJava.getClasspath(), moduleOptions);

        // when
        mutator.modularizeJavaCompileTask(compileJava);

        // then
        List<String> twoLastArguments = twoLastCompilerArgs(compileJava);
        assertEquals(
                Arrays.asList("--module-path", classpath.getAsPath()),
                twoLastArguments,
                "Two last arguments should be setting module path to the current compileJava task classpath");
    }

    private List<String> twoLastCompilerArgs(JavaCompile compileJava) {
        List<String> allCompilerArgs = compileJava.getOptions().getAllCompilerArgs();
        int size = allCompilerArgs.size();
        return allCompilerArgs.subList(size - 2, size);
    }

}
