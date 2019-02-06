package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.distribution.Distribution;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
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
import java.util.Collections;
import java.util.List;

public class RunTaskMutator {
    private static final String LIBS_PLACEHOLDER = "APP_HOME_LIBS_PLACEHOLDER";
    private static final String PATCH_LIBS_PLACEHOLDER = "APP_HOME_PATCH_LIBS_PLACEHOLDER";
    private static final Logger LOGGER = Logging.getLogger(RunTaskMutator.class);

    private final JavaExec execTask;
    private final Project project;
    private final String moduleName;

    public RunTaskMutator(JavaExec execTask, Project project, String moduleName) {
        this.execTask = execTask;
        this.project = project;
        this.moduleName = moduleName;
    }

    public void configureRun() {
        execTask.getExtensions().create("moduleOptions", ModuleOptions.class, project);
        updateJavaExecTask();
    }

    public void updateStartScriptsTask(String taskStartScriptsName) {
        CreateStartScripts startScriptsTask = (CreateStartScripts) project.getTasks().findByName(taskStartScriptsName);
        if (startScriptsTask == null)
            throw new IllegalArgumentException("Task " + taskStartScriptsName + " not found.");
        updateStartScriptsTask(startScriptsTask);
    }

    public void updateStartScriptsTask(CreateStartScripts startScriptsTask) {
        PatchModuleExtension patchModuleExtension = project.getExtensions().getByType(PatchModuleExtension.class);

        startScriptsTask.doFirst(new Action<Task>() {
            @Override
            public void execute(final Task task) {
                startScriptsTask.setClasspath(project.files());
                if (ModularCreateStartScripts.UNDEFINED_MAIN_CLASS_NAME.equals(startScriptsTask.getMainClassName())) {
                    startScriptsTask.setMainClassName(/* moduleName + "/" + */ execTask.getMain());
                }

                var moduleJvmArgs = List.of(
                        "--module-path", LIBS_PLACEHOLDER,
                        "--module", RunTaskMutator.this.getMainClass()
                );

                var jvmArgs = new ArrayList<String>();

                ModuleOptions moduleOptions = execTask.getExtensions().getByType(ModuleOptions.class);
                if (!moduleOptions.getAddModules().isEmpty()) {
                    String addModules = String.join(",", moduleOptions.getAddModules());
                    jvmArgs.add("--add-modules");
                    jvmArgs.add(addModules);
                }

                patchModuleExtension.getConfig().forEach(patch -> {
                            String[] split = patch.split("=");
                            jvmArgs.add("--patch-module");
                            jvmArgs.add(split[0] + "=" + PATCH_LIBS_PLACEHOLDER + "/" + split[1]);
                        }
                );

                startScriptsTask.getDefaultJvmOpts().forEach(jvmArgs::add);
                jvmArgs.addAll(moduleJvmArgs);

                startScriptsTask.setDefaultJvmOpts(jvmArgs);
            }
        });

        startScriptsTask.doLast(new Action<Task>() {
            @Override
            public void execute(final Task task) {
                File bashScript = new File(startScriptsTask.getOutputDir(), startScriptsTask.getApplicationName());
                RunTaskMutator.this.replaceLibsPlaceHolder(bashScript.toPath(), "\\$APP_HOME/lib", "\\$APP_HOME/patchlibs");
                File batFile = new File(startScriptsTask.getOutputDir(), startScriptsTask.getApplicationName() + ".bat");
                RunTaskMutator.this.replaceLibsPlaceHolder(batFile.toPath(), "%APP_HOME%\\\\lib", "%APP_HOME%\\\\patchlibs");
            }
        });
    }

    public void movePatchedLibs() {
        PatchModuleExtension patchModuleExtension = project.getExtensions().getByType(PatchModuleExtension.class);

        if(!patchModuleExtension.getConfig().isEmpty()) {
            Distribution distribution = ((DistributionContainer) project.getExtensions().getByName("distributions")).getByName("main");
            distribution.contents(new Action<CopySpec>() {
                @Override
                public void execute(CopySpec copySpec) {
                    copySpec.filesMatching(patchModuleExtension.getJars(), (action) -> {
                        action.setRelativePath(action.getRelativePath().getParent().getParent().append(true, "patchlibs", action.getName()));
                    });
                }
            });
        }
    }

    private void updateJavaExecTask() {
        execTask.doFirst(new Action<Task>() {
            @Override
            public void execute(final Task task) {

                JavaPluginConvention javaConvention = execTask.getProject().getConvention().getPlugin(JavaPluginConvention.class);
                PatchModuleExtension patchModuleExtension = project.getExtensions().getByType(PatchModuleExtension.class);

                SourceSet mainSourceSet = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

                var moduleJvmArgs = List.of(
                        "--module-path", execTask.getClasspath()
                                .filter(patchModuleExtension::isUnpatched
                                ).getAsPath(),
                        "--patch-module", moduleName + "=" + mainSourceSet.getOutput().getResourcesDir().toPath(),
                        "--module", RunTaskMutator.this.getMainClass()
                );

                var jvmArgs = new ArrayList<String>();

                ModuleOptions moduleOptions = execTask.getExtensions().getByType(ModuleOptions.class);
                if (!moduleOptions.getAddModules().isEmpty()) {
                    String addModules = String.join(",", moduleOptions.getAddModules());
                    jvmArgs.add("--add-modules");
                    jvmArgs.add(addModules);
                }

                jvmArgs.addAll(patchModuleExtension.configure(execTask.getClasspath()));

                jvmArgs.addAll(execTask.getJvmArgs());
                jvmArgs.addAll(moduleJvmArgs);

                execTask.setClasspath(project.files());

                execTask.setJvmArgs(jvmArgs);
            }
        });
    }

    private String getMainClass() {
        String main;
        if (!execTask.getMain().contains("/")) {
            LOGGER.warn("No module was provided for main class, assuming the current module. Prefer providing 'mainClassName' in the following format: '$moduleName/a.b.Main'");
            main = moduleName + "/" + execTask.getMain();
        } else {
            main = execTask.getMain();
        }
        return main;
    }

    private void replaceLibsPlaceHolder(Path path, String libText, String patchLibText) {
        try {
            String bashScript = Files.readString(path, StandardCharsets.UTF_8);
            String updatedBashScript = bashScript.replaceAll(LIBS_PLACEHOLDER, libText);
            updatedBashScript = updatedBashScript.replaceAll(PATCH_LIBS_PLACEHOLDER, patchLibText);

            Files.write(path, updatedBashScript.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new GradleException("Couldn't replace placeholder in " + path);
        }
    }
}
