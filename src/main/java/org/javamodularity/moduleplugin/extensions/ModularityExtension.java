package org.javamodularity.moduleplugin.extensions;

/**
 * A project-wide extension that provides the most common modularity-related actions.
 *
 * @see DefaultModularityExtension
 */
public interface ModularityExtension {

    /**
     * Calling this method results in all Java classes being compiled to Java release 9+ (as given by the
     * {@code mainJavaRelease} parameter).
     * <p>
     * See details about the {@code --release} option
     * <a href="https://docs.oracle.com/en/java/javase/11/tools/javac.html">here</a>.
     *
     * @param mainJavaRelease value for the {@code --release} option of {@code compileJava} task (allowed range: 9+)
     */
    void standardJavaRelease(int mainJavaRelease);

    /**
     * Calling this method results in all Java classes being compiled to Java release 6-8 (as given by the
     * {@code mainJavaRelease} parameter), with the exception of {@code module-info.java} being compiled to
     * Java release 9.
     *
     * @param mainJavaRelease value for the {@code --release} option of {@code compileJava} task (allowed range: 6-8)
     */
    default void mixedJavaRelease(int mainJavaRelease) {
        mixedJavaRelease(mainJavaRelease, 9);
    }

    /**
     * Calling this method results in all Java classes being compiled to Java release 6-8 (as given by the
     * {@code mainJavaRelease} parameter), with the exception of {@code module-info.java} being compiled to
     * Java release 9+ (as given by the {@code moduleInfoJavaRelease} parameter).
     * <p>
     * See details about the {@code --release} option
     * <a href="https://docs.oracle.com/en/java/javase/11/tools/javac.html">here</a>.
     *
     * @param mainJavaRelease       value for the {@code --release} option of {@code compileJava} task
     *                              (allowed range: 6-8)
     * @param moduleInfoJavaRelease value for the {@code --release} option of {@code compileModuleInfoJava} task
     *                              (allowed range: 9+)
     */
    void mixedJavaRelease(int mainJavaRelease, int moduleInfoJavaRelease);


    OptionContainer optionContainer();

    default void patchModule(String moduleName, String jarName) {
        optionContainer().getPatchModuleContainer().addJar(moduleName, jarName);
    }

    /**
     * Calling this method improves the ".classpath"-file created by Gradle's eclipse-plugin.
     *
     * <p>This method configures the plugin such that the given content of a ".classpath"-file
     * is modified in the following ways:
     * <ol>
     *   <li>Each "classpathentry" of kind = "con" with a path containing "JRE_CONTAINER"
     *       is moved to the module-path.
     *   <li>Each "classpathentry" of kind = "lib" with a gradle_used_by_scope containing "main"
     *       gets an additional attribute module = "true".
     *   <li>Each "classpathentry" with a gradle_used_by_scope of "test"
     *       gets an additional attribute test = "true".
     * </ol>
     *
     * <p>For more information see Gradle's manual for the eclipse-plugin.
     */
    void improveEclipseClasspathFile();

    default void moduleVersion(String version) {
        optionContainer().setModuleVersion(version);
    }

    /**
     * Apply workaround for https://github.com/gradle/gradle/issues/11124 (see also: https://github.com/java9-modularity/gradle-modules-plugin/issues/65)
     */
    default void disableEffectiveArgumentsAdjustment() {
        optionContainer().setEffectiveArgumentsAdjustmentEnabled(false);
    }
}
