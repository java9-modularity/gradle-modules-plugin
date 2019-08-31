package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;
import org.javamodularity.moduleplugin.internal.CompileModuleInfoHelper;

import java.nio.file.Files;
import java.nio.file.Path;

public class CompileModuleInfoTask extends AbstractCompileTask {

    public CompileModuleInfoTask(Project project) {
        super(project);
    }

    /**
     * @see CompileTask#configureCompileJava()
     */
    public void configureCompileModuleInfoJava() {
        helper().findTask(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class)
                .ifPresent(this::configureCompileModuleInfoJava);
    }

    private void configureCompileModuleInfoJava(JavaCompile compileJava) {
        var moduleOptions = compileJava.getExtensions().getByType(CompileModuleOptions.class);
        project.afterEvaluate(p -> {
            if (moduleOptions.getCompileModuleInfoSeparately()) {
                configureModularityForCompileModuleInfoJava(compileJava, moduleOptions);
            }
        });
    }

    /**
     * @see CompileTask#configureModularityForCompileJava
     */
    void configureModularityForCompileModuleInfoJava(
            JavaCompile compileJava, CompileModuleOptions moduleOptions) {
        JavaCompile compileModuleInfoJava = preconfigureCompileModuleInfoJava(compileJava);
        CompileModuleInfoHelper.dependOnOtherCompileModuleInfoJavaTasks(compileModuleInfoJava);

        CompileJavaTaskMutator mutator = createCompileJavaTaskMutator(compileJava, moduleOptions);

        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        compileModuleInfoJava.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                mutator.modularizeJavaCompileTask(compileModuleInfoJava);
            }
        });

        project.getTasks().withType(Jar.class).configureEach(jar -> jar.from(helper().getModuleInfoDir()));
    }

    /**
     * Preconfigures a separate task that is meant to compile {@code module-info.java} separately.
     * Final (modular) configuration is performed later by {@link CompileJavaTaskMutator}.
     */
    private JavaCompile preconfigureCompileModuleInfoJava(JavaCompile compileJava) {
        var compileModuleInfoJava = helper().compileJavaTask(CompileModuleOptions.COMPILE_MODULE_INFO_TASK_NAME);

        compileModuleInfoJava.setClasspath(project.files()); // empty
        compileModuleInfoJava.setSource(pathToModuleInfoJava());
        compileModuleInfoJava.getOptions().setSourcepath(project.files(pathToModuleInfoJava().getParent()));

        compileModuleInfoJava.setDestinationDir(helper().getModuleInfoDir());

        // we need all the compiled classes before compiling module-info.java
        compileModuleInfoJava.dependsOn(compileJava);

        // make "classes" trigger module-info.java compilation
        helper().task(JavaPlugin.CLASSES_TASK_NAME).dependsOn(compileModuleInfoJava);

        return compileModuleInfoJava;
    }

    private Path pathToModuleInfoJava() {
        return helper().mainSourceSet().getJava().getSrcDirs().stream()
                .map(srcDir -> srcDir.toPath().resolve("module-info.java"))
                .filter(Files::exists)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("module-info.java not found"));
    }

}
