package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.JavaExec;
import org.gradle.util.GradleVersion;
import org.javamodularity.moduleplugin.extensions.PatchModuleContainer;
import org.javamodularity.moduleplugin.extensions.RunModuleOptions;
import org.javamodularity.moduleplugin.internal.TaskOption;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RunTaskMutator extends AbstractExecutionMutator {
    private static final Logger LOGGER = Logging.getLogger(RunTaskMutator.class);

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
        FileCollection filteredClasspath = project.files(classpath.getFiles().stream()
                .filter(f -> f.isDirectory() || f.getName().endsWith(".jar") || f.getName().endsWith(".jmod"))
                .collect(Collectors.toList()).toArray());
        var patchModuleContainer = PatchModuleContainer.copyOf(
                helper().modularityExtension().optionContainer().getPatchModuleContainer());
        var resourceDir = helper().mainSourceSet().getOutput().getResourcesDir();
        if (resourceDir != null && Files.isDirectory(resourceDir.toPath())) {
            patchModuleContainer.addDir(moduleName, resourceDir.getAbsolutePath());
        }
        patchModuleContainer.buildModulePathOption(filteredClasspath).ifPresent(option -> option.mutateArgs(jvmArgs));
        patchModuleContainer.mutator(filteredClasspath).mutateArgs(jvmArgs);

        jvmArgs.addAll(execTask.getJvmArgs());

        if(GradleVersion.current().compareTo(GradleVersion.version("6.4")) < 0) {
            new TaskOption("--module", getMainClassName()).mutateArgs(jvmArgs);
        }

        LOGGER.info("jvmArgs for task {}: {}", execTask.getName(), jvmArgs);

        return jvmArgs;
    }
}
