package org.javamodularity.moduleplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.javamodularity.moduleplugin.tasks.*;

public class ModuleSystemPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        new ModuleName().findModuleName(project).ifPresent(moduleName -> configureModularity(project, moduleName));
    }

    private void configureModularity(Project project, String moduleName) {
        ExtensionContainer extensions = project.getExtensions();
        extensions.add("moduleName", moduleName);
        extensions.create("patchModules", PatchModuleExtension.class);

        new CompileTask(project).configureCompileJava();
        new CompileModuleInfoTask(project).configureCompileModuleInfoJava();
        new CompileTestTask().configureCompileTestJava(project, moduleName);
        new TestTask().configureTestJava(project, moduleName);
        new RunTask().configureRun(project, moduleName);
        new JavadocTask().configureJavaDoc(project);
        ModularJavaExec.configure(project, moduleName);
        ModularCreateStartScripts.configure(project, moduleName);
    }
}
