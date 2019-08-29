package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.javamodularity.moduleplugin.extensions.ModuleOptions;
import org.javamodularity.moduleplugin.extensions.PatchModuleExtension;
import org.javamodularity.moduleplugin.internal.StreamHelper;

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
        FileCollection classpath = mergeClassesHelper().getMergeAdjustedClasspath(javadoc.getClasspath());

        StreamHelper.concat(
                patchModuleExtension.buildModulePathOption(classpath).stream(),
                patchModuleExtension.resolvePatched(classpath).buildOptionStream(),
                moduleOptions.buildFullOptionStream()
        ).forEach(option -> option.mutateOptions(options));
    }

}
