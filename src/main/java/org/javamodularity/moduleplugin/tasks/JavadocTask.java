package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.external.javadoc.JavadocOptionFileOption;
import org.gradle.external.javadoc.internal.MultilineStringsJavadocOptionFileOption;
import org.javamodularity.moduleplugin.extensions.JavadocModuleOptions;
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
        var moduleOptions = javadoc.getExtensions().create("moduleOptions", JavadocModuleOptions.class, project);

        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        javadoc.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                addJavadocOptions(javadoc, moduleOptions);
                javadoc.setClasspath(project.files());
            }
        });
    }

    private void addJavadocOptions(Javadoc javadoc, JavadocModuleOptions moduleOptions) {
        var options = (CoreJavadocOptions) javadoc.getOptions();
        FileCollection classpath = mergeClassesHelper().getMergeAdjustedClasspath(javadoc.getClasspath());

        JavadocOptionFileOption<?>[] addExportsOption = new JavadocOptionFileOption<?>[] { null };
        var patchModuleContainer = helper().modularityExtension().optionContainer().getPatchModuleContainer();
        StreamHelper.concat(
                patchModuleContainer.buildModulePathOption(classpath).stream(),
                patchModuleContainer.mutator(classpath).taskOptionStream(),
                moduleOptions.buildFullOptionStreamLogged()
        ).forEach(option -> {
            if(option.getJavadocFlag().equals("-add-exports")) {
                if(addExportsOption[0] == null) {
                    addExportsOption[0] = options.addMultilineStringsOption("-add-exports");
                }
                var opt = (MultilineStringsJavadocOptionFileOption)addExportsOption[0];
                opt.getValue().add(option.getValue());
            } else {
                option.mutateOptions(options);
            }
        });
    }

}
