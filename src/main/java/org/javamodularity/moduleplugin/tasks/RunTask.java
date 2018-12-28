package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.tasks.JavaExec;

public class RunTask {
    public void configureRun(Project project, String moduleName) {
        project.getPluginManager().withPlugin(ApplicationPlugin.APPLICATION_PLUGIN_NAME, plugin -> {
            if (project.getPlugins().hasPlugin("application")) {
                JavaExec execTask = (JavaExec) project.getTasks().findByName(ApplicationPlugin.TASK_RUN_NAME);
                var mutator = new RunTaskMutator(execTask, project, moduleName);
                mutator.configureRun();

                project.afterEvaluate(p -> {
                    mutator.updateStartScriptsTask(ApplicationPlugin.TASK_START_SCRIPTS_NAME);
                    mutator.movePatchedLibs();
                });
            }
        });
    }


}
