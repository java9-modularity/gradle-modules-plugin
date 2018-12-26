package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.distribution.Distribution;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.file.CopySpec;
import org.gradle.api.plugins.ApplicationPluginConvention;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.application.CreateStartScripts;

import java.io.File;
import java.util.ArrayList;

public class ModularCreateStartScripts extends CreateStartScripts {
    public static final String UNDEFINED_MAIN_CLASS_NAME = "<undefined>";

    @Internal
    private ModularJavaExec runTask;

    public ModularCreateStartScripts() {
        setClasspath(getProject().files());
        setMainClassName(UNDEFINED_MAIN_CLASS_NAME);
    }

    public ModularJavaExec getRunTask() {
        return runTask;
    }

    public void setRunTask(ModularJavaExec runTask) {
        this.runTask = runTask;
    }

    public static void configure(Project project, String moduleName) {
        project.afterEvaluate(p -> {
            project.getTasks().withType(ModularCreateStartScripts.class).forEach(startScriptsTask -> {
                var appConvention = project.getConvention().findPlugin(ApplicationPluginConvention.class);
                if(appConvention != null) {
                    var distDir = project.file("" + project.getBuildDir() + "/install/" + appConvention.getApplicationName());
                    startScriptsTask.setOutputDir(new File(distDir, appConvention.getExecutableDir()));
                }

                ModularJavaExec runTask = startScriptsTask.getRunTask();
                if(runTask == null) throw new GradleException("runTask not set for task " + startScriptsTask.getName());
                var runJvmArgs = runTask.getOwnJvmArgs();
                if(!runJvmArgs.isEmpty()) {
                    var jvmArgs = new ArrayList<>(runJvmArgs);
                    startScriptsTask.getDefaultJvmOpts().forEach(jvmArgs::add);
                    startScriptsTask.setDefaultJvmOpts(jvmArgs);
                }

                var mutator = new RunTaskMutator(runTask, project, moduleName);
                mutator.updateStartScriptsTask(startScriptsTask);
                mutator.movePatchedLibs();
            });
        });
    }
}
