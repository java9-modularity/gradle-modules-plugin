package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.JavaExec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RunTaskMutator extends AbstractExecutionMutator {

    public RunTaskMutator(JavaExec execTask, Project project) {
        super(execTask, project);
    }

    public void configureRun() {
        execTask.getExtensions().create("moduleOptions", ModuleOptions.class, project);
        updateJavaExecTask();
    }

    private void updateJavaExecTask() {
        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        execTask.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                List<String> jvmArgs = buildJavaExecJvmArgs();
                execTask.setJvmArgs(jvmArgs);
                execTask.setClasspath(project.files());
            }
        });
    }

    private List<String> buildJavaExecJvmArgs() {
        String moduleName = helper().moduleName();
        var patchModuleExtension = helper().extension(PatchModuleExtension.class);

        var moduleJvmArgs = List.of(
                "--module-path",
                patchModuleExtension.getUnpatchedClasspathAsPath(execTask.getClasspath()),
                "--patch-module",
                moduleName + "=" + helper().mainSourceSet().getOutput().getResourcesDir().toPath(),
                "--module",
                getMainClassName()
        );

        var jvmArgs = new ArrayList<String>();

        ModuleOptions moduleOptions = execTask.getExtensions().getByType(ModuleOptions.class);
        if (!moduleOptions.getAddModules().isEmpty()) {
            String addModules = String.join(",", moduleOptions.getAddModules());
            Stream.of("--add-modules", addModules).forEach(jvmArgs::add);
        }

        patchModuleExtension.resolve(execTask.getClasspath()).toArgumentStream().forEach(jvmArgs::add);

        jvmArgs.addAll(execTask.getJvmArgs());
        jvmArgs.addAll(moduleJvmArgs);
        return jvmArgs;
    }
}
