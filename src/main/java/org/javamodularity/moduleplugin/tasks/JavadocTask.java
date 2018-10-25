package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;

public class JavadocTask {
    public void configureJavaDoc(Project project) {
        Javadoc javadoc = (Javadoc) project.getTasks().findByName(JavaPlugin.JAVADOC_TASK_NAME);
        if (javadoc != null) {

            javadoc.doFirst(task -> {
                CoreJavadocOptions options = (CoreJavadocOptions) javadoc.getOptions();
                options.addStringOption("-module-path", javadoc.getClasspath().getAsPath());
                javadoc.setClasspath(project.files());
            });

        }

    }
}
