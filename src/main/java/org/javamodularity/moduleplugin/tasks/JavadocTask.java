package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;

public class JavadocTask {
    public void configureJavaDoc(Project project) {
        Javadoc javadoc = (Javadoc) project.getTasks().findByName(JavaPlugin.JAVADOC_TASK_NAME);
        if (javadoc != null) {

            javadoc.getExtensions().create("moduleOptions", ModuleOptions.class, project);
            PatchModuleExtension patchModuleExtension = project.getExtensions().getByType(PatchModuleExtension.class);

            // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
            javadoc.doFirst(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    ModuleOptions moduleOptions = javadoc.getExtensions().getByType(ModuleOptions.class);
                    CoreJavadocOptions options = (CoreJavadocOptions) javadoc.getOptions();
                    options.addStringOption("-module-path", javadoc.getClasspath()
                            .filter(patchModuleExtension::isUnpatched)
                            .getAsPath());

                    if (!moduleOptions.getAddModules().isEmpty()) {
                        String addModules = String.join(",", moduleOptions.getAddModules());
                        options.addStringOption("-add-modules", addModules);
                    }

                    patchModuleExtension.getConfig().forEach(patch -> {
                                String[] split = patch.split("=");

                                String asPath = javadoc.getClasspath().filter(jar -> jar.getName().endsWith(split[1])).getAsPath();

                                if (asPath != null && asPath.length() > 0) {
                                    options.addStringOption("-patch-module", split[0] + "=" + asPath);
                                }

                            }
                    );

                    javadoc.setClasspath(project.files());
                }

            });
        }

    }
}
