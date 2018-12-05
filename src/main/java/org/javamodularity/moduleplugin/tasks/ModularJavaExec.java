package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;

public class ModularJavaExec extends JavaExec {
    public static void configure(Project project, String moduleName) {
        project.afterEvaluate(p -> {
            project.getTasks().withType(ModularJavaExec.class).forEach(execTask -> {
                if(execTask.getClasspath().isEmpty()) {
                    var javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
                    var main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                    execTask.classpath(main.getRuntimeClasspath());
                }
                var mutator = new RunTaskMutator(execTask, project, moduleName);
                mutator.configureRun();
            });
        });
    }
}
