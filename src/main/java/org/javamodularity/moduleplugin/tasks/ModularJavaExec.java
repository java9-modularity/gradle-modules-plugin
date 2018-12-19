package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;

import java.util.ArrayList;
import java.util.List;

public class ModularJavaExec extends JavaExec {
    @Internal
    private final List<String> ownJvmArgs = new ArrayList<>();

    public static void configure(Project project, String moduleName) {
        project.afterEvaluate(p -> {
            project.getTasks().withType(ModularJavaExec.class).forEach(execTask -> {
                if(execTask.getClasspath().isEmpty()) {
                    var javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
                    var main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                    execTask.classpath(main.getRuntimeClasspath());
                }
                var mutator = new RunTaskMutator(execTask, project, moduleName);
                mutator.configureRun();
            });
        });
    }

    List<String> getOwnJvmArgs() {
        return ownJvmArgs;
    }

    @Override
    public JavaExec jvmArgs(Object... arguments) {
        for(Object arg : arguments) {
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
}
