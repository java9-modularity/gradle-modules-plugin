package org.javamodularity.moduleplugin.extensions;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.javamodularity.moduleplugin.tasks.ClasspathFile;

/**
 * This class provides options for {@link ClasspathFile}.
 *
 * <p>An example on how to write an extension could be found here:
 * <a href="https://guides.gradle.org/implementing-gradle-plugins/">Gradle Guide</a>
 *
 * @author <a href="mailto:alfred.65.fiedler@gmail.com">Alfred Fiedler</a>
 */
// Note: Don't make this class final because Gradle will extend it
public class ClasspathFileExtension {
  /**
   * Default value for {@link #improveClasspathFile}.
   *
   * <p>For the default value (i.e. the value if not set or changed by a build-script)
   * the following possibilities exist:
   * <ul>
   *   <li><b>TRUE</b>: In this case the {@code .classpath} file will be improved if not
   *       "opted-out" by the build-script. In the long run this might be the preferred
   *       value for build-script authors.<br>
   *       Con: Users of this plugin who already improve the {@code .classpath} file today have
   *       to "opt-out" after updating {@code gradle-modules-plugin}, because their improvement
   *       might interfere with the improvement in the updated version. In other words: The build
   *       of a user of this plugin might fail after updating the {@code gradle-module-plugin}.
   *   <li><b>FALSE</b>: In this case the {@code .classpath} file will not be improved by newer
   *       versions of {@code gradle-modules-plugin} unless "opted-in" by a build-script.<br>
   *       Pro: Full backward compatibility to version 1.6.0 of {@code gradle-modules-plugin}.<br>
   *       Con: If the default value is changed in the future from {@code false} to {@code true}
   *       then every build-srcipt "opting-in" contains a no longer necessary statement "opt-in".
   *       This is a very moderate drawback.
   * </ul>
   *
   * <p>For the reasons stated above {@code false} is chosen as default value.
   */
  /* package */ static final boolean DEFAULT_VALUE = false; // NOPMD avoid redundant initializer */

  /**
   * Flag indicating whether the {@code .classpath} file should be improved (true) or not (false).
   */
  private final Property<Boolean> improveClasspathFile; // */

  /**
   * Constructor.
   *
   * @param project
   *        this extension is associated with
   */
  public ClasspathFileExtension(final Project project) {
    improveClasspathFile = project.getObjects().property(Boolean.class);
    improveClasspathFile.set(DEFAULT_VALUE);
  } // end method */

  /**
   * Returns {@link Property} indicating whether or not {@code gradle-modules-plugin} will
   * improve the {@code .classpath} file when task "eclipseClasspath" is executed.
   *
   * <p><i><b>Note 1: </b>
   * Use {@link Property#get()} for reading the value of the returned {@link Property}.
   * </i>
   *
   * <p><i><b>Note 2: </b>
   * Use {@link Property#set(Object)} to change the value of the returned {@link Property}.
   * </i>
   *
   * @return {@code improveClasspathFile} {@link Property}, the value of that {@link Property} is
   *         true if {@code gradle-modules-plugin} improves {@code .classpath} file,
   *         false otherwise
   */
  public final Property<Boolean> getImproveClasspathFile() {
    return improveClasspathFile;
  } // end method */
} // end class