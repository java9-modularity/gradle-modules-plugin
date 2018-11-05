package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.application.CreateStartScripts;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RunTask {

    private static final String LIBS_PLACEHOLDER = "APP_HOME_LIBS_PLACEHOLDER";
    private static final Logger LOGGER = Logging.getLogger(RunTask.class);

    public void configureRun(Project project, String moduleName) {
        project.getPluginManager().withPlugin(ApplicationPlugin.APPLICATION_PLUGIN_NAME, plugin -> {
            if (project.getPlugins().hasPlugin("application")) {
                JavaExec execTask = (JavaExec) project.getTasks().findByName(ApplicationPlugin.TASK_RUN_NAME);
                execTask.getExtensions().create("moduleOptions", ModuleOptions.class, project);
                updateJavaExecTask(execTask, moduleName);
                updateStartScriptsTask(project, execTask, moduleName);
            }
        });
    }

    private void updateStartScriptsTask(Project project, JavaExec execTask, String moduleName) {
        CreateStartScripts startScriptsTask = (CreateStartScripts) project.getTasks().findByName(ApplicationPlugin.TASK_START_SCRIPTS_NAME);
        startScriptsTask.doFirst(task -> {
            startScriptsTask.setClasspath(project.files());

            var moduleJvmArgs = new ArrayList<>(List.of(
                    "--module-path", LIBS_PLACEHOLDER)
            );
            if (!moduleName.isEmpty()) {
                moduleJvmArgs.addAll(List.of(
                        "--module", getMainClass(moduleName, execTask))
                );
            }

            var jvmArgs = new ArrayList<String>();

            ModuleOptions moduleOptions = execTask.getExtensions().getByType(ModuleOptions.class);
            if(!moduleOptions.getAddModules().isEmpty()) {
                String addModules = String.join(",", moduleOptions.getAddModules());
                jvmArgs.add("--add-modules");
                jvmArgs.add(addModules);
            }
            startScriptsTask.getDefaultJvmOpts().forEach(jvmArgs::add);
            jvmArgs.addAll(moduleJvmArgs);

            startScriptsTask.setDefaultJvmOpts(jvmArgs);
        });

        startScriptsTask.doLast(task -> {
            File bashScript = new File(startScriptsTask.getOutputDir(), startScriptsTask.getApplicationName());
            replaceLibsPlaceHolder(bashScript.toPath(), "\\$APP_HOME/lib");
            File batFile = new File(startScriptsTask.getOutputDir(), startScriptsTask.getApplicationName() + ".bat");
            replaceLibsPlaceHolder(batFile.toPath(), "%APP_HOME%\\\\lib");
        });
    }

    private void updateJavaExecTask(JavaExec execTask, String moduleName) {
        execTask.doFirst(task -> {

            JavaPluginConvention javaConvention = execTask.getProject().getConvention().getPlugin(JavaPluginConvention.class);

            SourceSet mainSourceSet = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

            var moduleJvmArgs = new ArrayList<>(List.of(
                    "--module-path", execTask.getClasspath().getAsPath(),
                    "--patch-module", moduleName + "=" + mainSourceSet.getOutput().getResourcesDir().toPath())
            );
            if (!moduleName.isEmpty()) {
                moduleJvmArgs.addAll(List.of(
                        "--module", getMainClass(moduleName, execTask))
                );
            }

            var jvmArgs = new ArrayList<String>();

            ModuleOptions moduleOptions = execTask.getExtensions().getByType(ModuleOptions.class);
            if(!moduleOptions.getAddModules().isEmpty()) {
                String addModules = String.join(",", moduleOptions.getAddModules());
                jvmArgs.add("--add-modules");
                jvmArgs.add(addModules);
            }

            jvmArgs.addAll(execTask.getJvmArgs());
            jvmArgs.addAll(moduleJvmArgs);



            execTask.setJvmArgs(jvmArgs);

        });
    }

    private String getMainClass(String moduleName, JavaExec execTask) {
        String main;
        if (!execTask.getMain().contains("/")) {
            LOGGER.warn("No module was provided for main class, assuming the current module. Prefer providing 'mainClassName' in the following format: '$moduleName/a.b.Main'");
            main = moduleName + "/" + execTask.getMain();
        } else {
            main = execTask.getMain();
        }
        return main;
    }

    private void replaceLibsPlaceHolder(Path path, String newText) {
        try {
            String bashScript = Files.readString(path, StandardCharsets.UTF_8);
            String updatedBashScript = bashScript.replaceAll(LIBS_PLACEHOLDER, newText);

            Files.write(path, updatedBashScript.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new GradleException("Couldn't replace placeholder in " + path);
        }
    }

}
