package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.util.GradleVersion;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;
import org.javamodularity.moduleplugin.internal.CompileModuleInfoHelper;

import java.util.Objects;
import java.util.Optional;

public class CompileTask extends AbstractCompileTask {
    private static final Logger LOGGER = Logging.getLogger(CompileTask.class);

    public CompileTask(Project project) {
        super(project);
    }

    /**
     * @see CompileModuleInfoTask#configureCompileModuleInfoJava()
     */
    public void configureCompileJava() {
        enforceJarForCompilation();
        helper().findTask(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class)
                .ifPresent(this::configureCompileJava);
    }

    private void configureCompileJava(JavaCompile compileJava) {
        if(GradleVersion.current().compareTo(GradleVersion.version("6.4")) >= 0) {
            compileJava.getModularity().getInferModulePath().set(false);
        }
        var moduleOptions = compileJava.getExtensions().create("moduleOptions", CompileModuleOptions.class, project);
        project.afterEvaluate(p -> {
            adjustMainClass(compileJava);

            MergeClassesHelper.POST_JAVA_COMPILE_TASK_NAMES.stream()
                    .map(name -> helper().findTask(name, AbstractCompile.class))
                    .flatMap(Optional::stream)
                    .filter(task -> !task.getSource().isEmpty())
                    .findAny()
                    .ifPresent(task -> moduleOptions.setCompileModuleInfoSeparately(true));

            if (moduleOptions.getCompileModuleInfoSeparately()) {
                compileJava.exclude("module-info.java");
            } else {
                configureModularityForCompileJava(compileJava, moduleOptions);
            }
        });
    }

    private void adjustMainClass(JavaCompile compileJava) {
        if(GradleVersion.current().compareTo(GradleVersion.version("6.4")) >= 0) {
            Property<String> mainClassProp = compileJava.getOptions().getJavaModuleMainClass();
            String mainClass = mainClassProp.getOrNull();
            if(mainClass != null) {
                int idx = mainClass.indexOf('/');
                if(idx >= 0) {
                    mainClassProp.set(mainClass.substring(idx + 1));
                }
            }
        }
    }

    // see https://github.com/gradle/gradle/issues/890#issuecomment-623392772 and issue #143
    private void enforceJarForCompilation() {
        Configuration config = project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);
        config.attributes(new Action<AttributeContainer>() {
            @Override
            public void execute(AttributeContainer attributeContainer) {
                if(GradleVersion.current().compareTo(GradleVersion.version("5.6")) < 0) {
                    LOGGER.warn("Cannot enforce using JARs for compilation. Please upgrade to Gradle 5.6 or newer.");
                    return;
                }
                attributeContainer.attribute(
                        LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                        project.getObjects().named(LibraryElements.class, LibraryElements.JAR));
            }
        });
    }

    /**
     * @see CompileModuleInfoTask#configureModularityForCompileModuleInfoJava
     */
    void configureModularityForCompileJava(JavaCompile compileJava, CompileModuleOptions moduleOptions) {
        CompileModuleInfoHelper.dependOnOtherCompileModuleInfoJavaTasks(compileJava);

        CompileJavaTaskMutator mutator = createCompileJavaTaskMutator(compileJava, moduleOptions);
        // don't convert to lambda: https://github.com/java9-modularity/gradle-modules-plugin/issues/54
        compileJava.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                mutator.modularizeJavaCompileTask(compileJava);
            }
        });
    }

}
