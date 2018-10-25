package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;

public class CompileTask {

    public void configureCompileJava(Project project) {
        JavaCompile compileJava = (JavaCompile) project.getTasks().findByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);
        if (compileJava != null) {
            compileJava.getExtensions().create("moduleOptions", ModuleOptions.class, project);

            compileJava.doFirst(task -> CompileJavaTaskMutator.mutateJavaCompileTask(project, compileJava));
        }
    }

}
