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
import org.gradle.util.GradleVersion;
import org.javamodularity.moduleplugin.extensions.RunModuleOptions;
import org.javamodularity.moduleplugin.internal.TaskOption;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StartScriptsMutator extends AbstractExecutionMutator {
    private static final Logger LOGGER = Logging.getLogger(StartScriptsMutator.class);

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

        if(GradleVersion.current().compareTo(GradleVersion.version("6.4")) < 0) {
            if (ModularCreateStartScripts.UNDEFINED_MAIN_CLASS_NAME.equals(startScriptsTask.getMainClassName())) {
                startScriptsTask.setMainClassName(execTask.getMain());
            }
        }
    }

    private List<String> buildStartScriptsJvmArgs(CreateStartScripts startScriptsTask) {
        var jvmArgs = new ArrayList<String>();

        var moduleOptions = execTask.getExtensions().getByType(RunModuleOptions.class);

        moduleOptions.mutateArgs(jvmArgs);

        var patchModuleContainer = helper().modularityExtension().optionContainer().getPatchModuleContainer();
        patchModuleContainer.mutator(jarName -> PATCH_LIBS_PLACEHOLDER + "/" + jarName).mutateArgs(jvmArgs);

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
        var patchedJarNames = helper().modularityExtension().optionContainer().getPatchModuleContainer().patchedJarNames();
        if (patchedJarNames.isEmpty()) {
            return;
        }

        Distribution mainDistribution = helper().extension("distributions", DistributionContainer.class)
                .getByName("main");
        mainDistribution.contents(
                copySpec -> copySpec.filesMatching(patchedJarNames, this::updateRelativePath)
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

        if(GradleVersion.current().compareTo(GradleVersion.version("7.2")) < 0) {
            replaceScriptContent(bashScript, "eval set -- \\$DEFAULT_JVM_OPTS \\$JAVA_OPTS \\$(\\S+).*", "eval set -- \\$JAVA_OPTS \\$$1 \\$DEFAULT_JVM_OPTS \\\"\\$APP_ARGS\\\"");
        } else {
            replaceScriptContent(bashScript, "app_path=\\$0", "app_path=\\$0\nAPP_ARGS=`echo \"\\$@\"`");
            String appOpts = getAppOptsVariableName(bashScript);
            replaceScriptContent(bashScript, "exec \"\\$JAVACMD\" \"\\$@\"",
                    "eval set -- \\$JAVA_OPTS " + appOpts + " \\$DEFAULT_JVM_OPTS \\\"\\$APP_ARGS\\\"\nexec \"\\$JAVACMD\" \"\\$@\"");
        }

        Path batFile = outputDir.resolve(startScriptsTask.getApplicationName() + ".bat");
//        replaceScriptContent(batFile, "\"%JAVA_EXE%\" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %(\\S+)%.*", "\"%JAVA_EXE%\" %JAVA_OPTS% %$1% %DEFAULT_JVM_OPTS% %CMD_LINE_ARGS%");
        replaceScriptContent(batFile, "\"%JAVA_EXE%\" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %(\\S+)%.*", "\"%JAVA_EXE%\" %JAVA_OPTS% %$1% %DEFAULT_JVM_OPTS% %*");
    }

    private static void replaceLibsPlaceHolder(Path path, String libText, String patchLibText) {
        try {
            String content = getContent(path);
            String updatedScriptContent = content
                    .replaceAll(LIBS_PLACEHOLDER, libText)
                    .replaceAll(PATCH_LIBS_PLACEHOLDER, patchLibText);

            Files.writeString(path, updatedScriptContent);
        } catch (IOException e) {
            throw new GradleException("Couldn't replace placeholder in " + path, e);
        }
    }

    private static String getContent(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            try {
                Charset cs = Charset.forName(System.getProperty("file.encoding"));
                return Files.readString(path, cs);
            } catch (Exception ex) {
                throw new GradleException("Cannot read the content of " + path, ex);
            }
        }
    }

    private static final Pattern APP_OPTS_VARIABLE_NAME = Pattern.compile("(?s).*\\$DEFAULT_JVM_OPTS \\$JAVA_OPTS \\$(\\w+).*");
    private static String getAppOptsVariableName(Path path) {
        try {
            var m = APP_OPTS_VARIABLE_NAME.matcher(Files.readString(path));
            if(m.matches()) {
                return "\\$" + m.group(1);
            }
        } catch (IOException e) {
            throw new GradleException("Couldn't read run script in " + path);
        }
        return "";
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
