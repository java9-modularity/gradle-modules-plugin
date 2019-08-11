package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;

import java.io.File;

public class CompileTask extends AbstractCompileTask {
    private static final String COMPILE_KOTLIN_TASK_NAME = "compileKotlin";
    private static final String COMPILE_GROOVY_TASK_NAME = "compileGroovy";

    public CompileTask(Project project) {
        super(project);
    }

    /**
     * @see CompileModuleInfoTask#configureCompileModuleInfoJava()
     */
    public void configureCompileJava() {
        helper().findTask(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class)
                .ifPresent(this::configureCompileJava);
    }

    private void configureCompileJava(JavaCompile compileJava) {
        var moduleOptions = compileJava.getExtensions().create("moduleOptions", CompileModuleOptions.class, project);
        project.afterEvaluate(p -> {
            helper().findTask(COMPILE_GROOVY_TASK_NAME, GroovyCompile.class)
                    .ifPresent(compileGroovy -> {
                        if(!helper().groovyMainSourceSet().getGroovy().getFiles().isEmpty()) {
                            moduleOptions.setCompileModuleInfoSeparately(true);
                            compileGroovy.setDestinationDir(compileJava.getDestinationDir());
                        }
                    });

            helper().findTask(COMPILE_KOTLIN_TASK_NAME, AbstractCompile.class)
                    .ifPresent(compileKotlin -> {
                        boolean hasKt = helper().kotlinMainSourceDirectorySet().getFiles().stream()
                                .map(File::getName)
                                .anyMatch(name -> name.endsWith(".kt"));
                        boolean hasJava = helper().mainSourceSet().getJava().getFiles().stream()
                                .map(File::getName)
                                .filter(name -> ! name.equals("module-info.java"))
                                .anyMatch(name -> name.endsWith(".java"));
                        if(hasKt && !hasJava) {
                            moduleOptions.setCompileModuleInfoSeparately(true);
                            compileJava.setDestinationDir(compileKotlin.getDestinationDir());
                        }
                    });

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
