package org.javamodularity.moduleplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.javamodularity.moduleplugin.extensions.DefaultModularityExtension;
import org.javamodularity.moduleplugin.extensions.ModularityExtension;
import org.javamodularity.moduleplugin.extensions.PatchModuleExtension;
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
        extensions.create(ModularityExtension.class, "modularity", DefaultModularityExtension.class, project);

        new CompileTask(project).configureCompileJava();
        new CompileModuleInfoTask(project).configureCompileModuleInfoJava();
        new MergeClassesTask(project).configureMergeClasses();
        new CompileTestTask(project).configureCompileTestJava();
        new TestTask(project).configureTestJava();
        new RunTask(project).configureRun();
        new JavadocTask(project).configureJavaDoc();
        new ClasspathFile(project).configure(); // improve .classpath-file
        ModularJavaExec.configure(project);
        ModularCreateStartScripts.configure(project);
    }
}
