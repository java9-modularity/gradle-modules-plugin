package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.TestEngine;

import java.util.ArrayList;
import java.util.List;

public class CompileTestTask {

    public void configureCompileTestJava(Project project, String moduleName) {
        JavaCompile compileTestJava = (JavaCompile) project.getTasks().findByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME);
        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        PatchModuleExtension patchModuleExtension = project.getExtensions().getByType(PatchModuleExtension.class);

        compileTestJava.getExtensions().create("moduleOptions", ModuleOptions.class, project);
        SourceSet testSourceSet = javaConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        compileTestJava.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                var args = new ArrayList<>(compileTestJava.getOptions().getCompilerArgs());
                args.addAll(List.of(
                        "--module-path", compileTestJava.getClasspath()
                                .filter(patchModuleExtension::isUnpatched)
                                .getAsPath(),
                        "--patch-module", moduleName + "=" + testSourceSet.getJava().getSourceDirectories().getAsPath()
                ));

                TestEngine.select(project).ifPresent(testEngine -> args.addAll(List.of(
                        "--add-modules", testEngine.moduleName,
                        "--add-reads", moduleName + "=" + testEngine.moduleName
                )));

                ModuleOptions moduleOptions = compileTestJava.getExtensions().getByType(ModuleOptions.class);
                moduleOptions.mutateArgs(moduleName, args);

                args.addAll(patchModuleExtension.configure(compileTestJava.getClasspath()));

                ModuleInfoTestHelper.mutateArgs(project, moduleName, args::add);

                compileTestJava.getOptions().setCompilerArgs(args);
                compileTestJava.setClasspath(project.files());
            }

        });
    }

}
