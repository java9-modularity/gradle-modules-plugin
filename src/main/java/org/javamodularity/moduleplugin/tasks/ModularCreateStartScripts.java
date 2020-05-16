package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPluginConvention;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.application.CreateStartScripts;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;

public class ModularCreateStartScripts extends CreateStartScripts {
    public static final String UNDEFINED_MAIN_CLASS_NAME = "<undefined>";

    private String changedMain;

    @Internal
    private ModularJavaExec runTask;

    public ModularCreateStartScripts() {
        setClasspath(getProject().files());
    }

    @Nullable
    @Override
    public String getMainClassName() {
        String main = changedMain;
        if(main == null) {
            main = super.getMainClassName();
        }
        if(main == null) {
            main = UNDEFINED_MAIN_CLASS_NAME;
        }
        return main;
    }

    @Override
    public void setMainClassName(@Nullable String mainClassName) {
        changedMain = mainClassName;
    }

    public ModularJavaExec getRunTask() {
        return runTask;
    }

    public void setRunTask(ModularJavaExec runTask) {
        this.runTask = runTask;
    }

    //region CONFIGURE
    public static void configure(Project project) {
        project.afterEvaluate(ModularCreateStartScripts::configureAfterEvaluate);
    }

    private static void configureAfterEvaluate(Project project) {
        project.getTasks().withType(ModularCreateStartScripts.class)
                .forEach(startScriptsTask -> configure(startScriptsTask, project));
    }

    private static void configure(ModularCreateStartScripts startScriptsTask, Project project) {
        var appConvention = project.getConvention().findPlugin(ApplicationPluginConvention.class);
        if (appConvention != null) {
            var distDir = project.file(project.getBuildDir() + "/install/" + appConvention.getApplicationName());
            startScriptsTask.setOutputDir(new File(distDir, appConvention.getExecutableDir()));
        }

        ModularJavaExec runTask = startScriptsTask.getRunTask();
        if (runTask == null) throw new GradleException("runTask not set for task " + startScriptsTask.getName());

        var runJvmArgs = runTask.getOwnJvmArgs();
        if (!runJvmArgs.isEmpty()) {
            var defaultJvmOpts = new ArrayList<>(runJvmArgs);
            startScriptsTask.getDefaultJvmOpts().forEach(defaultJvmOpts::add);
            startScriptsTask.setDefaultJvmOpts(defaultJvmOpts);
        }

        var mutator = new StartScriptsMutator(runTask, project);
        mutator.updateStartScriptsTask(startScriptsTask);
        mutator.movePatchedLibs();
    }
    //endregion
}
