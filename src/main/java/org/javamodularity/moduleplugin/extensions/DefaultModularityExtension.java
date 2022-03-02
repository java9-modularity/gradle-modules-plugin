package org.javamodularity.moduleplugin.extensions;

import java.util.List;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.util.GradleVersion;
import org.javamodularity.moduleplugin.JavaProjectHelper;
import org.javamodularity.moduleplugin.tasks.ClasspathFile;

public class DefaultModularityExtension implements ModularityExtension {

    private final Project project;
    private final OptionContainer optionContainer = new OptionContainer();

    public DefaultModularityExtension(Project project) {
        this.project = project;
    }

    @Override
    public void standardJavaRelease(int mainJavaRelease) {
        if (mainJavaRelease < 9) {
            throw new IllegalArgumentException(String.format(
                    "Invalid main --release value: %d. Use 'mixedJavaRelease' instead.", mainJavaRelease
            ));
        }
        project.afterEvaluate(p -> configureStandardJavaRelease(mainJavaRelease));
    }

    private void configureStandardJavaRelease(int mainJavaRelease) {
        JavaCompile compileJava = helper().compileJavaTask(JavaPlugin.COMPILE_JAVA_TASK_NAME);
        setJavaRelease(compileJava, mainJavaRelease);
    }

    @Override
    public void mixedJavaRelease(int mainJavaRelease, int moduleInfoJavaRelease) {
        validateMixedJavaReleaseArgs(mainJavaRelease, moduleInfoJavaRelease);

        CompileModuleOptions moduleOptions = helper().compileJavaTask(JavaPlugin.COMPILE_JAVA_TASK_NAME)
                .getExtensions().getByType(CompileModuleOptions.class);
        moduleOptions.setCompileModuleInfoSeparately(true);

        project.afterEvaluate(p -> configureMixedJavaRelease(mainJavaRelease, moduleInfoJavaRelease));
    }

    @Override
    public OptionContainer optionContainer() {
        return optionContainer;
    }

    private static void validateMixedJavaReleaseArgs(int mainJavaRelease, int moduleInfoJavaRelease) {
        if (mainJavaRelease < 6) {
            throw new IllegalArgumentException("Invalid main --release value: " + mainJavaRelease);
        }
        if (mainJavaRelease > 8) {
            throw new IllegalArgumentException(String.format(
                    "Invalid main --release value: %d. Use 'standardJavaRelease' instead.", mainJavaRelease
            ));
        }
        if (moduleInfoJavaRelease < 9) {
            throw new IllegalArgumentException("Invalid module-info --release value: " + moduleInfoJavaRelease);
        }
    }

    private void configureMixedJavaRelease(int mainJavaRelease, int moduleInfoJavaRelease) {
        var compileJava = helper().compileJavaTask(JavaPlugin.COMPILE_JAVA_TASK_NAME);
        setJavaRelease(compileJava, mainJavaRelease);

        var compileModuleInfoJava = helper().compileJavaTask(CompileModuleOptions.COMPILE_MODULE_INFO_TASK_NAME);
        setJavaRelease(compileModuleInfoJava, moduleInfoJavaRelease);
    }

    // TODO: Remove this method when Gradle supports it natively:  https://github.com/gradle/gradle/issues/2510
    private void setJavaRelease(JavaCompile javaCompile, int javaRelease) {
        String currentJavaVersion = JavaVersion.current().toString();
        if (!javaCompile.getSourceCompatibility().equals(currentJavaVersion)) {
            throw new IllegalStateException("sourceCompatibility should not be set together with --release option");
        }
        if (!javaCompile.getTargetCompatibility().equals(currentJavaVersion)) {
            throw new IllegalStateException("targetCompatibility should not be set together with --release option");
        }

        List<String> compilerArgs = javaCompile.getOptions().getCompilerArgs();
        if (compilerArgs.contains("--release")) {
            throw new IllegalStateException("--release option is already set in compiler args");
        }

        if (releaseOptionIsSupported()) {
            // using the `convention(Integer)` method instead of the `set(Integer)` method, to let users set overwrite explicitly
            javaCompile.getOptions().getRelease().convention(javaRelease);
        } else {
            compilerArgs.add("--release");
            compilerArgs.add(String.valueOf(javaRelease));
        }
    }

    /**
     * @see <a href="https://github.com/gradle/gradle/issues/2510#issuecomment-657436188">The comment on GitHub issue that says {@code --release} option is added in Gradle 6.6</a>
     * @return true if the version of Gradle is 6.6 or later
     */
    private boolean releaseOptionIsSupported() {
        boolean supported = GradleVersion.current().compareTo(GradleVersion.version("6.6")) >= 0;
        project.getLogger().debug("Is releaseOptionSupported? {}", supported);
        return supported;
    }

    private JavaProjectHelper helper() {
        return new JavaProjectHelper(project);
    }

    @Override
    public void improveEclipseClasspathFile() {
        project.afterEvaluate(p -> {
            helper().findTask("eclipseClasspath", Task.class)
                    .ifPresent(new ClasspathFile()::configure);

        });
    }
}
