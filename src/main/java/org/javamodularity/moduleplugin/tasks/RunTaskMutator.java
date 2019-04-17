package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.distribution.Distribution;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.file.RelativePath;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.javamodularity.moduleplugin.JavaProjectHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class RunTaskMutator {
    private static final String LIBS_PLACEHOLDER = "APP_HOME_LIBS_PLACEHOLDER";
    private static final String PATCH_LIBS_PLACEHOLDER = "APP_HOME_PATCH_LIBS_PLACEHOLDER";
    private static final Logger LOGGER = Logging.getLogger(RunTaskMutator.class);

    private final JavaExec execTask;
    private final Project project;

    public RunTaskMutator(JavaExec execTask, Project project) {
        this.execTask = execTask;
        this.project = project;
    }

    public void configureRun() {
        execTask.getExtensions().create("moduleOptions", ModuleOptions.class, project);
        updateJavaExecTask();
    }

    public void updateStartScriptsTask(String taskStartScriptsName) {
        CreateStartScripts startScriptsTask = helper().task(taskStartScriptsName, CreateStartScripts.class);
        updateStartScriptsTask(startScriptsTask);
    }

    public void updateStartScriptsTask(CreateStartScripts startScriptsTask) {

        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        startScriptsTask.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                configureStartScriptsDoFirst(startScriptsTask);
            }
        });

        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        startScriptsTask.doLast(new Action<Task>() {
            @Override
            public void execute(Task task) {
                configureStartScriptsDoLast(startScriptsTask);
            }
        });
    }

    private void configureStartScriptsDoFirst(CreateStartScripts startScriptsTask) {
        List<String> jvmArgs = buildStartScriptsJvmArgs(startScriptsTask);
        startScriptsTask.setDefaultJvmOpts(jvmArgs);
        startScriptsTask.setClasspath(project.files());

        if (ModularCreateStartScripts.UNDEFINED_MAIN_CLASS_NAME.equals(startScriptsTask.getMainClassName())) {
            startScriptsTask.setMainClassName(/* helper().moduleName() + "/" + */ execTask.getMain());
        }
    }

    private List<String> buildStartScriptsJvmArgs(CreateStartScripts startScriptsTask) {
        var jvmArgs = new ArrayList<String>();

        String moduleName = helper().moduleName();
        var patchModuleExtension = helper().extension(PatchModuleExtension.class);

        var moduleJvmArgs = List.of(
                "--module-path", LIBS_PLACEHOLDER,
                "--module", getMainClassName()
        );

        ModuleOptions moduleOptions = execTask.getExtensions().getByType(ModuleOptions.class);
        moduleOptions.mutateArgs(moduleName, jvmArgs);

        buildPatchModuleArgStream(patchModuleExtension).forEach(jvmArgs::add);

        startScriptsTask.getDefaultJvmOpts().forEach(jvmArgs::add);

        jvmArgs.addAll(moduleJvmArgs);

        return jvmArgs;
    }

    private Stream<String> buildPatchModuleArgStream(PatchModuleExtension patchModuleExtension) {
        return patchModuleExtension.resolve(jarName -> PATCH_LIBS_PLACEHOLDER + "/" + jarName).toArgumentStream();
    }

    private void configureStartScriptsDoLast(CreateStartScripts startScriptsTask) {
        Path outputDir = startScriptsTask.getOutputDir().toPath();

        Path bashScript = outputDir.resolve(startScriptsTask.getApplicationName());
        replaceLibsPlaceHolder(bashScript, "\\$APP_HOME/lib", "\\$APP_HOME/patchlibs");

        Path batFile = outputDir.resolve(startScriptsTask.getApplicationName() + ".bat");
        replaceLibsPlaceHolder(batFile, "%APP_HOME%\\\\lib", "%APP_HOME%\\\\patchlibs");
    }

    public void movePatchedLibs() {
        var patchModuleExtension = helper().extension(PatchModuleExtension.class);
        if (patchModuleExtension.getConfig().isEmpty()) {
            return;
        }

        Distribution mainDistribution = helper().extension("distributions", DistributionContainer.class)
                .getByName("main");
        mainDistribution.contents(
                copySpec -> copySpec.filesMatching(patchModuleExtension.getJars(), this::updateRelativePath)
        );
    }

    private void updateRelativePath(FileCopyDetails fileCopyDetails) {
        RelativePath updatedRelativePath = fileCopyDetails.getRelativePath().getParent().getParent()
                .append(true, "patchlibs", fileCopyDetails.getName());
        fileCopyDetails.setRelativePath(updatedRelativePath);
    }

    private void updateJavaExecTask() {
        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        execTask.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                List<String> jvmArgs = buildJavaExecJvmArgs();
                execTask.setJvmArgs(jvmArgs);
                execTask.setClasspath(project.files());
            }
        });
    }

    private List<String> buildJavaExecJvmArgs() {
        String moduleName = helper().moduleName();
        var patchModuleExtension = helper().extension(PatchModuleExtension.class);

        var moduleJvmArgs = List.of(
                "--module-path",
                patchModuleExtension.getUnpatchedClasspathAsPath(execTask.getClasspath()),
                "--patch-module",
                moduleName + "=" + helper().mainSourceSet().getOutput().getResourcesDir().toPath(),
                "--module",
                getMainClassName()
        );

        var jvmArgs = new ArrayList<String>();

        ModuleOptions moduleOptions = execTask.getExtensions().getByType(ModuleOptions.class);
        if (!moduleOptions.getAddModules().isEmpty()) {
            String addModules = String.join(",", moduleOptions.getAddModules());
            Stream.of("--add-modules", addModules).forEach(jvmArgs::add);
        }

        patchModuleExtension.resolve(execTask.getClasspath()).toArgumentStream().forEach(jvmArgs::add);

        jvmArgs.addAll(execTask.getJvmArgs());
        jvmArgs.addAll(moduleJvmArgs);
        return jvmArgs;
    }

    private String getMainClassName() {
        String mainClassName = Objects.requireNonNull(execTask.getMain());
        if (!mainClassName.contains("/")) {
            LOGGER.warn("No module was provided for main class, assuming the current module. Prefer providing 'mainClassName' in the following format: '$moduleName/a.b.Main'");
            return helper().moduleName() + "/" + mainClassName;
        }
        return mainClassName;
    }

    private static void replaceLibsPlaceHolder(Path path, String libText, String patchLibText) {
        try {
            String bashScript = Files.readString(path);
            String updatedBashScript = bashScript
                    .replaceAll(LIBS_PLACEHOLDER, libText)
                    .replaceAll(PATCH_LIBS_PLACEHOLDER, patchLibText);

            Files.writeString(path, updatedBashScript);
        } catch (IOException e) {
            throw new GradleException("Couldn't replace placeholder in " + path);
        }
    }

    private JavaProjectHelper helper() {
        return new JavaProjectHelper(project);
    }
}
