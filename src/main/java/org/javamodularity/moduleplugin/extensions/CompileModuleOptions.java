package org.javamodularity.moduleplugin.extensions;

import org.gradle.api.Project;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.internal.TaskOption;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class CompileModuleOptions extends ModuleOptions {

    /**
     * Name of the extra Java compile task created if {@code compileModuleInfoSeparately} is {@code true}.
     */
    public static final String COMPILE_MODULE_INFO_TASK_NAME = "compileModuleInfoJava";

    private final Project project;

    private boolean compileModuleInfoSeparately = false;

    public CompileModuleOptions(Project project) {
        super(project);
        this.project = project;
    }

    public boolean getCompileModuleInfoSeparately() {
        return compileModuleInfoSeparately;
    }

    public void setCompileModuleInfoSeparately(boolean compileModuleInfoSeparately) {
        if (compileModuleInfoSeparately) {
            // we need to create "compileModuleInfoJava" task eagerly so that the user can configure it immediately
            project.getTasks().maybeCreate(COMPILE_MODULE_INFO_TASK_NAME, JavaCompile.class);
        }
        this.compileModuleInfoSeparately = compileModuleInfoSeparately;
    }
}
