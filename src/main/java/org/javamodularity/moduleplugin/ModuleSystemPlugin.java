package org.javamodularity.moduleplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.util.GradleVersion;
import org.javamodularity.moduleplugin.extensions.DefaultModularityExtension;
import org.javamodularity.moduleplugin.extensions.ModularityExtension;
import org.javamodularity.moduleplugin.extensions.PatchModuleExtension;
import org.javamodularity.moduleplugin.extensions.PatchModuleContainer;
import org.javamodularity.moduleplugin.tasks.*;

public class ModuleSystemPlugin implements Plugin<Project> {
    private static final Logger LOGGER = Logging.getLogger(ModuleSystemPlugin.class);

    @Override
    public void apply(Project project) {
        if(GradleVersion.current().compareTo(GradleVersion.version("5.1")) < 0) {
            LOGGER.warn("WARNING: You use " + GradleVersion.current() +
                    ". The minimum version supported (with some limitations) by this plugin is 5.1." +
                    "  It is strongly recommended to use at least Gradle 5.6.");
        }
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
        ModularJavaExec.configure(project);
        ModularCreateStartScripts.configure(project);
        PatchModuleContainer.configure(project);
    }
}
