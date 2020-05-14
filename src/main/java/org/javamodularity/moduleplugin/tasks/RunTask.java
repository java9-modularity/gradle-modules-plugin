package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.tasks.JavaExec;

public class RunTask extends AbstractModulePluginTask {

    public RunTask(Project project) {
        super(project);
    }

    public void configureRun() {
        project.getPluginManager().withPlugin(ApplicationPlugin.APPLICATION_PLUGIN_NAME, plugin -> doConfigureRun());
    }

    private void doConfigureRun() {
        if(helper().shouldFixEffectiveArguments()) {
            project.getTasks().replace(ApplicationPlugin.TASK_RUN_NAME, ModularJavaExec.class);
        }
        var mutator = new RunTaskMutator(getRunTask(), project);
        mutator.configureRun();
        project.afterEvaluate(p -> configureStartScripts());
    }

    private void configureStartScripts() {
        var mutator = new StartScriptsMutator(getRunTask(), project);
        mutator.updateStartScriptsTask(ApplicationPlugin.TASK_START_SCRIPTS_NAME);
        mutator.movePatchedLibs();
    }

    private JavaExec getRunTask() {
        return helper().task(ApplicationPlugin.TASK_RUN_NAME, JavaExec.class);
    }
}
