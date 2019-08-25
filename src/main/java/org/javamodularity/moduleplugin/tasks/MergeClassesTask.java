package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaPlugin;

import java.util.Optional;
import java.util.stream.Stream;

public class MergeClassesTask extends AbstractModulePluginTask {
    public MergeClassesTask(Project project) {
        super(project);
    }

    public void configureMergeClasses() {
        project.afterEvaluate(p -> configureMergeClassesAfterEvaluate());
    }

    public void configureMergeClassesAfterEvaluate() {
        if (!mergeClassesHelper().isMergeRequired()) {
            return;
        }

        var mergeClasses = mergeClassesHelper().createMergeClassesTask();

        mergeClassesHelper().allCompileTaskStream().forEach(task -> {
            mergeClasses.from(task.getDestinationDir());
            mergeClasses.dependsOn(task);
        });
        mergeClasses.into(helper().getMergedDir());

        Stream.of(ApplicationPlugin.TASK_RUN_NAME, JavaPlugin.TEST_TASK_NAME)
                .map(helper()::findTask)
                .flatMap(Optional::stream)
                .forEach(task -> task.dependsOn(mergeClasses));
    }
}
