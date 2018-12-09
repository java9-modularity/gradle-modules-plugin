package org.javamodularity.moduleplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.javamodularity.moduleplugin.tasks.*;

import java.util.Optional;

public class ModuleSystemPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        Optional<String> foundModuleName = new ModuleName().findModuleName(project);
        foundModuleName.ifPresent(moduleName -> {
            project.getExtensions().add("moduleName", moduleName);
            new CompileTask().configureCompileJava(project);
            new CompileTestTask().configureCompileTestJava(project, moduleName);
            new TestTask().configureTestJava(project, moduleName);
            new RunTask().configureRun(project, moduleName);
            new JavadocTask().configureJavaDoc(project);
            ModularJavaExec.configure(project, moduleName);
            ModularCreateStartScripts.configure(project, moduleName);
        });
    }
}
