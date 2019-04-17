package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.javamodularity.moduleplugin.JavaProjectHelper;

import java.util.ArrayList;
import java.util.List;

public class ModularJavaExec extends JavaExec {
    @Internal
    private final List<String> ownJvmArgs = new ArrayList<>();

    List<String> getOwnJvmArgs() {
        return ownJvmArgs;
    }

    @Override
    public JavaExec jvmArgs(Object... arguments) {
        for (Object arg : arguments) {
            ownJvmArgs.add(String.valueOf(arg));
        }
        return super.jvmArgs(arguments);
    }

    @Override
    public JavaExec jvmArgs(Iterable<?> arguments) {
        arguments.forEach(arg -> ownJvmArgs.add(String.valueOf(arg)));
        return super.jvmArgs(arguments);
    }

    @Override
    public void setJvmArgs(List<String> arguments) {
        ownJvmArgs.clear();
        ownJvmArgs.addAll(arguments);
        super.setJvmArgs(arguments);
    }

    @Override
    public void setJvmArgs(Iterable<?> arguments) {
        ownJvmArgs.clear();
        arguments.forEach(arg -> ownJvmArgs.add(String.valueOf(arg)));
        super.setJvmArgs(arguments);
    }

    //region CONFIGURE
    public static void configure(Project project) {
        project.afterEvaluate(ModularJavaExec::configureAfterEvaluate);
    }

    private static void configureAfterEvaluate(Project project) {
        project.getTasks().withType(ModularJavaExec.class).forEach(execTask -> configure(execTask, project));
    }

    private static void configure(ModularJavaExec execTask, Project project) {
        if (execTask.getClasspath().isEmpty()) {
            var mainSourceSet = new JavaProjectHelper(project).mainSourceSet();
            execTask.classpath(mainSourceSet.getRuntimeClasspath());
        }

        var mutator = new RunTaskMutator(execTask, project);
        mutator.configureRun();
    }
    //endregion
}
