package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;

public class JavadocTask extends AbstractModulePluginTask {

    public JavadocTask(Project project) {
        super(project);
    }

    public void configureJavaDoc() {
        helper().findTask(JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class)
                .ifPresent(this::configureJavaDoc);
    }

    private void configureJavaDoc(Javadoc javadoc) {
        var moduleOptions = javadoc.getExtensions().create("moduleOptions", ModuleOptions.class, project);

        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        javadoc.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                addJavadocOptions(javadoc, moduleOptions);
                javadoc.setClasspath(project.files());
            }
        });
    }

    private void addJavadocOptions(Javadoc javadoc, ModuleOptions moduleOptions) {
        var options = (CoreJavadocOptions) javadoc.getOptions();
        var patchModuleExtension = helper().extension(PatchModuleExtension.class);

        String modulePath = patchModuleExtension.getUnpatchedClasspathAsPath(javadoc.getClasspath());
        options.addStringOption("-module-path", modulePath);

        if (!moduleOptions.getAddModules().isEmpty()) {
            String addModules = String.join(",", moduleOptions.getAddModules());
            options.addStringOption("-add-modules", addModules);
        }

        patchModuleExtension.resolve(javadoc.getClasspath()).toValueStream()
                .forEach(value -> options.addStringOption("-patch-module", value));
    }
}
