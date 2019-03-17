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
}
