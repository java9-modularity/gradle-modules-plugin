package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.process.ExecResult;
import org.gradle.util.GradleVersion;
import org.javamodularity.moduleplugin.JavaProjectHelper;

import java.util.ArrayList;
import java.util.List;

import static org.joor.Reflect.on;
import static org.joor.Reflect.onClass;

public class ModularJavaExec extends JavaExec {
    private static final Logger LOGGER = Logging.getLogger(ModularJavaExec.class);

    @Internal
    private final List<String> ownJvmArgs = new ArrayList<>();

    public ModularJavaExec() {
        if(GradleVersion.current().compareTo(GradleVersion.version("6.4")) >= 0) {
            this.getModularity().getInferModulePath().set(true);
        }
    }

    List<String> getOwnJvmArgs() {
        return ownJvmArgs;
    }

    @Override
    public JavaExec jvmArgs(Object... arguments) {
        for (Object arg : arguments) {
            ownJvmArgs.add(String.valueOf(arg));
        }
        return super.jvmArgs(arguments);
    }

    @Override
    public JavaExec jvmArgs(Iterable<?> arguments) {
        arguments.forEach(arg -> ownJvmArgs.add(String.valueOf(arg)));
        return super.jvmArgs(arguments);
    }

    @Override
    public void setJvmArgs(List<String> arguments) {
        ownJvmArgs.clear();
        ownJvmArgs.addAll(arguments);
        super.setJvmArgs(arguments);
    }

    @Override
    public void setJvmArgs(Iterable<?> arguments) {
        ownJvmArgs.clear();
        arguments.forEach(arg -> ownJvmArgs.add(String.valueOf(arg)));
        super.setJvmArgs(arguments);
    }

    @Input
    @Override
    public String getMain() {
        if(GradleVersion.current().compareTo(GradleVersion.version("6.4")) >= 0) {
            return stripModule(getMainClass().getOrNull());
        } else {
            return super.getMain();
        }
    }

    @Override
    public JavaExec setMain(String mainClassName) {
        if(GradleVersion.current().compareTo(GradleVersion.version("6.4")) >= 0) {
            getMainClass().set(stripModule(mainClassName));
        } else {
            super.setMain(mainClassName);
        }
        return this;
    }

    private static String stripModule(String main) {
        if(main == null) return main;
        int idx = main.indexOf('/');
        return (idx < 0) ? main : main.substring(idx + 1);
    }

    @TaskAction
    public void exec() {
        if(new JavaProjectHelper(getProject()).shouldFixEffectiveArguments()) {
            execFixEffectiveArguments();
        } else {
            super.exec();
        }
    }

    private void execFixEffectiveArguments() {
        this.setJvmArgs(this.getJvmArgs());

        var hb = on(this).field("javaExecHandleBuilder").get();
        var handleBuilder = on(hb);
        String executable = handleBuilder.call("getExecutable").get();
        if (executable == null || executable.isEmpty()) throw new IllegalStateException("execCommand == null!");

        List<String> arguments = handleBuilder.field("javaOptions").call("getAllJvmArgs").get();
        LOGGER.info("run: raw jvmArgs = " + arguments);
        int idx = arguments.lastIndexOf("--module");
        if(idx < 0) {
            idx = arguments.lastIndexOf("-m");
        }
        if(idx >= 0 && idx < arguments.size() - 2) {
            List<String> fixedArgs = new ArrayList<>(arguments.subList(0, idx));
            fixedArgs.addAll(arguments.subList(idx + 2, arguments.size()));
            fixedArgs.addAll(arguments.subList(idx, idx+2));
            arguments = fixedArgs;
        }
        LOGGER.info("run: adjusted jvmArgs = " + arguments);
        if(idx < 0) {
            arguments.add(getMain());
        }

        arguments.addAll(getArgs());
        for (CommandLineArgumentProvider provider : getArgumentProviders()) {
            provider.asArguments().forEach(arguments::add);
        }
        LOGGER.info("run: effectiveArgs = " + arguments);

        var execHandle = onClass("org.gradle.process.internal.DefaultExecHandle").create(
                handleBuilder.call("getDisplayName").get(),
                handleBuilder.call("getWorkingDir").get(),
                executable,
                arguments,
                handleBuilder.call("getActualEnvironment").get(),
                handleBuilder.call("getEffectiveStreamsHandler").get(),
                handleBuilder.field("inputHandler").get(),
                handleBuilder.field("listeners").get(),
                handleBuilder.field("redirectErrorStream").get(),
                handleBuilder.field("timeoutMillis").get(),
                handleBuilder.field("daemon").get(),
                handleBuilder.field("executor").get(),
                handleBuilder.field("buildCancellationToken").get()
        );
        execHandle.call("start");
        ExecResult execResult = execHandle.call("waitForFinish").get();
        if (!this.isIgnoreExitValue()) {
            execResult.assertNormalExitValue();
        }
        if(GradleVersion.current().compareTo(GradleVersion.version("6.1")) >= 0) {
            ((Property<ExecResult>)this.getExecutionResult()).set(execResult);
        }
    }

    //region CONFIGURE
    public static void configure(Project project) {
        project.afterEvaluate(ModularJavaExec::configureAfterEvaluate);
    }

    private static void configureAfterEvaluate(Project project) {
        project.getTasks().withType(ModularJavaExec.class).forEach(execTask -> {
            if(!execTask.getName().equals(ApplicationPlugin.TASK_RUN_NAME)) {
                configure(execTask, project);
            }
        });
    }

    private static void configure(ModularJavaExec execTask, Project project) {
        if (execTask.getClasspath().isEmpty()) {
            var mainSourceSet = new JavaProjectHelper(project).mainSourceSet();
            execTask.classpath(mainSourceSet.getRuntimeClasspath());
        }

        var mutator = new RunTaskMutator(execTask, project);
        mutator.configureRun();
    }
    //endregion
}
