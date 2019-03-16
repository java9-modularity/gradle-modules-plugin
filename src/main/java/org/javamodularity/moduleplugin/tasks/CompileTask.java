package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;

public class CompileTask extends AbstractCompileTask {

    public CompileTask(Project project) {
        super(project);
    }

    /**
     * @see CompileModuleInfoTask#configureCompileModuleInfoJava()
     */
    public void configureCompileJava() {
        helper().findCompileJavaTask(JavaPlugin.COMPILE_JAVA_TASK_NAME)
                .ifPresent(this::configureCompileJava);
    }

    private void configureCompileJava(JavaCompile compileJava) {
        var moduleOptions = compileJava.getExtensions().create("moduleOptions", CompileModuleOptions.class, project);
        project.afterEvaluate(p -> {
            if (moduleOptions.getCompileModuleInfoSeparately()) {
                compileJava.exclude("module-info.java");
            } else {
                configureModularityForCompileJava(compileJava, moduleOptions);
            }
        });
    }

    /**
     * @see CompileModuleInfoTask#configureModularityForCompileModuleInfoJava
     */
    void configureModularityForCompileJava(JavaCompile compileJava, CompileModuleOptions moduleOptions) {
        CompileJavaTaskMutator mutator = createCompileJavaTaskMutator(compileJava, moduleOptions);
        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        compileJava.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                mutator.modularizeJavaCompileTask(compileJava);
            }
        });
    }

}
