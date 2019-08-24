package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.distribution.Distribution;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.file.RelativePath;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.javamodularity.moduleplugin.internal.TaskOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StartScriptsMutator extends AbstractExecutionMutator {

    private static final String LIBS_PLACEHOLDER = "APP_HOME_LIBS_PLACEHOLDER";
    private static final String PATCH_LIBS_PLACEHOLDER = "APP_HOME_PATCH_LIBS_PLACEHOLDER";

    public StartScriptsMutator(JavaExec execTask, Project project) {
        super(execTask, project);
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
               configureStartScriptsDoLast((CreateStartScripts) task);
            }
        });
    }

    private void configureStartScriptsDoLast(CreateStartScripts startScriptsTask) {
        replaceLibsPlaceHolders(startScriptsTask);
        removeClasspathArgs(startScriptsTask);
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

        var patchModuleExtension = helper().extension(PatchModuleExtension.class);
        var moduleOptions = execTask.getExtensions().getByType(ModuleOptions.class);

        moduleOptions.mutateArgs(jvmArgs);

        patchModuleExtension.resolvePatched(jarName -> PATCH_LIBS_PLACEHOLDER + "/" + jarName).mutateArgs(jvmArgs);

        startScriptsTask.getDefaultJvmOpts().forEach(jvmArgs::add);

        new TaskOption("--module-path", LIBS_PLACEHOLDER).mutateArgs(jvmArgs);
        new TaskOption("--module", getMainClassName()).mutateArgs(jvmArgs);

        return jvmArgs;
    }

    private void replaceLibsPlaceHolders(CreateStartScripts startScriptsTask) {
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

    private void removeClasspathArgs(CreateStartScripts startScriptsTask) {
        Path outputDir = startScriptsTask.getOutputDir().toPath();
        Path bashScript = outputDir.resolve(startScriptsTask.getApplicationName());

        replaceScriptContent(bashScript, "eval set .*", "eval set -- \\$DEFAULT_JVM_OPTS \\$JAVA_OPTS \\$CLI_OPTS \\\"\\$APP_ARGS\\\"");

        Path batFile = outputDir.resolve(startScriptsTask.getApplicationName() + ".bat");
        replaceScriptContent(batFile, "\"%JAVA_EXE%\" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %CLI_OPTS%.*", "\"%JAVA_EXE%\" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %CLI_OPTS% %CMD_LINE_ARGS%");
    }

    private static void replaceLibsPlaceHolder(Path path, String libText, String patchLibText) {
        try {
            String updatedScriptContent = Files.readString(path)
                    .replaceAll(LIBS_PLACEHOLDER, libText)
                    .replaceAll(PATCH_LIBS_PLACEHOLDER, patchLibText);

            Files.writeString(path, updatedScriptContent);
        } catch (IOException e) {
            throw new GradleException("Couldn't replace placeholder in " + path);
        }
    }

    private static void replaceScriptContent(Path path, String regex, String replacement) {
        try {
            String updatedScriptContent = Files.readString(path).replaceAll(regex, replacement);

            Files.writeString(path, updatedScriptContent);
        } catch (IOException e) {
            throw new GradleException("Couldn't update run script in " + path);
        }
    }
}
