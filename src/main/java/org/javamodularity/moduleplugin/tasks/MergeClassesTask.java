package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.javamodularity.moduleplugin.JavaProjectHelper;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MergeClassesTask extends AbstractModulePluginTask {
    private static final Logger LOGGER = Logging.getLogger(MergeClassesTask.class);

    public MergeClassesTask(Project project) {
        super(project);
    }

    public void configureMergeClasses() {
        project.afterEvaluate(p -> configureMergeClassesAfterEvaluate());
    }

    public void configureMergeClassesAfterEvaluate() {
        var mergeClasses = mergeClassesHelper().createMergeClassesTask();

        mergeClassesHelper().allCompileTaskStream().forEach(taskWrapper -> {
            List<String> modularTasks = List.of(JavaPlugin.COMPILE_JAVA_TASK_NAME, CompileModuleOptions.COMPILE_MODULE_INFO_TASK_NAME);
            if(modularTasks.contains(taskWrapper.getTask().getName())) {
                mergeClasses.from(taskWrapper.getDestinationDir());
            } else {
                mergeClasses.from(taskWrapper.getDestinationDir(), copySpec -> copySpec.exclude("**/module-info.class"));
            }
            mergeClasses.dependsOn(taskWrapper.getTask());
        });
        mergeClasses.into(helper().getMergedDir());
        mergeClasses.onlyIf(task -> mergeClassesHelper().isMergeRequired());

        Stream.of(ApplicationPlugin.TASK_RUN_NAME, JavaPlugin.TEST_TASK_NAME, JavaProjectHelper.COMPILE_TEST_FIXTURES_JAVA_TASK_NAME)
                .map(helper()::findTask)
                .flatMap(Optional::stream)
                .forEach(task -> task.dependsOn(mergeClasses));
    }
}
