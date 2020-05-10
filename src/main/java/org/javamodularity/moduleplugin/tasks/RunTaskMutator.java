package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.JavaExec;
import org.javamodularity.moduleplugin.extensions.RunModuleOptions;
import org.javamodularity.moduleplugin.extensions.PatchModuleContainer;
import org.javamodularity.moduleplugin.internal.TaskOption;

import java.util.ArrayList;
import java.util.List;

public class RunTaskMutator extends AbstractExecutionMutator {

    public RunTaskMutator(JavaExec execTask, Project project) {
        super(execTask, project);
    }

    public void configureRun() {
        execTask.getExtensions().create("moduleOptions", RunModuleOptions.class, project);
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
        var jvmArgs = new ArrayList<String>();

        String moduleName = helper().moduleName();
        var moduleOptions = execTask.getExtensions().getByType(RunModuleOptions.class);

        moduleOptions.mutateArgs(jvmArgs);

        FileCollection classpath = mergeClassesHelper().getMergeAdjustedClasspath(execTask.getClasspath());
        var patchModuleContainer = PatchModuleContainer.copyOf(
                helper().modularityExtension().optionContainer().getPatchModuleContainer());
        patchModuleContainer.addDir(moduleName, helper().mainSourceSet().getOutput().getResourcesDir().getAbsolutePath());
        patchModuleContainer.buildModulePathOption(classpath).ifPresent(option -> option.mutateArgs(jvmArgs));
        patchModuleContainer.mutator(classpath).mutateArgs(jvmArgs);

        jvmArgs.addAll(execTask.getJvmArgs());

        new TaskOption("--module", getMainClassName()).mutateArgs(jvmArgs);

        return jvmArgs;
    }
}
