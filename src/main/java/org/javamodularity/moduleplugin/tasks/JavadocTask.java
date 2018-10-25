package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;

public class JavadocTask {
    public void configureJavaDoc(Project project) {
        Javadoc javadoc = (Javadoc) project.getTasks().findByName(JavaPlugin.JAVADOC_TASK_NAME);
        if (javadoc != null) {

            javadoc.getExtensions().create("moduleOptions", ModuleOptions.class, project);

            javadoc.doFirst(task -> {
                ModuleOptions moduleOptions = javadoc.getExtensions().getByType(ModuleOptions.class);

                CoreJavadocOptions options = (CoreJavadocOptions) javadoc.getOptions();
                options.addStringOption("-module-path", javadoc.getClasspath().getAsPath());

                if(!moduleOptions.getAddModules().isEmpty()) {
                    String addModules = String.join(",", moduleOptions.getAddModules());
                    options.addStringOption("-add-module", addModules);
                }

                javadoc.setClasspath(project.files());
            });

        }

    }
}
