package org.javamodularity.moduleplugin.tasks;

import groovy.util.Node;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.plugins.ide.eclipse.GenerateEclipseClasspath;
import org.gradle.plugins.ide.eclipse.model.EclipseClasspath;

/**
 * This class provides functionality to improve the content of {@code .classpath} file created
 * by the Gradle's eclipse-plugin.
 *
 * @author <a href="mailto:alfred.65.fiedler@gmail.com">Alfred Fiedler</a>
 */
public final class ClasspathFile {
  /**
   * Logger.
   */
  private static final Logger LOGGER = Logging.getLogger(ClasspathFile.class); // */

  /**
   * Name of interesting items when it comes to investigation.
   */
  /* package */ static final String NAME_ITEM = "classpathentry"; // */

  /**
   * Name of interesting child in item.
   */
  /* package */ static final String NAME_CHILD = "attributes"; // */

  /**
   * Name of interesting child in child of item.
   */
  /* package */ static final String NAME_GRAND = "attribute"; // */

  /**
   * Name of interesting attribute in child of child of item.
   */
  /* package */ static final String NAME_ATTRIBUTE = "gradle_used_by_scope"; // */

  /**
   * Part of path indicating that JRE is configured.
   */
  /* package */ static final String NAME_JRE = "JRE_CONTAINER"; // */

  /**
   * Configures appropriate action if project has task "eclipseClasspath".
   *
   * @param task
   *        responsible for generating {@code .classpath} file
   */
  public void configure(final Task task) {
    // LOGGER.quiet("configure, task: {}", task.getClass());

    // --- add functionality for enhancing the content of ".classpath"-file
    // Note: For more information see the manual for the eclipse-plugin.
    final EclipseClasspath eclipseClasspath = ((GenerateEclipseClasspath) task).getClasspath();
    eclipseClasspath.file(
        xmlFileContentMerger -> {
          xmlFileContentMerger.withXml(
              xmlProvider -> {
                final Node rootNode = xmlProvider.asNode();

                // show content of .classpath file before improving
                LOGGER.debug("addAction: rootNode.before improving:{}", rootNode);

                // improve
                improveEclipseClasspathFile(rootNode);

                // show content of .classpath file after improving
                LOGGER.debug("addAction: rootNode.after  improving:{}", rootNode);
              } // end xmlProvider's lambda expression
          );
        } // end xmlFileContentMerger's lambda expression
    );
  } // end method */

  /**
   * Improve ".classpath"-file.
   *
   * <p>This method modifies the given content of a ".classpath"-file in the following ways:
   * <ol>
   *   <li>Each "classpathentry" of kind = "con" with a path containing "JRE_CONTAINER"
   *       is moved to the module-path.
   *   <li>Each "classpathentry" of kind = "lib" with a gradle_used_by_scope containing "main"
   *       gets an additional attribute module = "true".
   *   <li>Each "classpathentry" with a gradle_used_by_scope of "test"
   *       gets an additional attribute test = "true".
   * </ol>
   *
   * @param rootNode
   *        XML-content to be improved
   */
  /* package */ void improveEclipseClasspathFile(final Node rootNode) {
    putJreOnModulePath(rootNode);
    markMain(rootNode);
    markTest(rootNode);
  } // end method */

  /**
   * Marks everything which is kind of {@code lib} with a {@code gradle_used_by_scope} containing
   * {@code main} as {@code module}.
   *
   * <p>All children of {@code rootNode} with name {@link #NAME_CHILD} which have a child with
   * name {@link #NAME_GRAND} and an attribute with name {@link #NAME_ATTRIBUTE} which contains
   * {@code main} get an attribute {@code module="true"} if they don't already have such an
   * attribute.
   *
   * <p><i><b>Note:</b>
   * Currently Gradle's eclipse-plugin creates ".classpath" files such that
   * from Eclipse's point of view all libraries appear on the classpath.
   * Its seems appropriate to adjust things such that libraries with a gradle scope containing
   * "main" are changed such that they appear on the module-path from Eclipse's point of view.
   * This works at least with the following versions:<br>
   * - Gradle 5.3.1, Eclipse 2019-09<br>
   * - Gradle 5.6.4, Eclipse 2019-09<br>
   * - Gradle 6.0.1, Eclipse 2019-09<br>
   * </i>
   *
   * @param rootNode
   *        XML-content to be improved
   */
  /* package */ void markMain(final Node rootNode) {
    // Excerpt from a child which should be improved:
    // <classpathentry sourcepath="D:/.p/..." kind="lib" path="D:/.p/...">
    //   <attributes>
    //     <attribute name="gradle_used_by_scope" value="main,test"/>
    //   </attributes>
    // </classpathentry>

    rootNode.children().stream()                                // loop over all children
        .filter(i  -> i instanceof Node)                        // better safe than sorry
        .filter(i  -> NAME_ITEM.equals(((Node)i).name()))       // with name "classpathentry"
        .filter(i  -> isKindOf((Node)i, "lib"))                 // kind of "lib"
        .filter(i  -> getGradleScope((Node)i).contains("main")) // appropriate gradle scope
        .filter(i  -> hasNoAttributeModule((Node)i))            // without "module" information
        .forEach(i -> addAttribute((Node)i, "module"));         // add module="true" attribute
  } // end method */

  /**
   * Marks everything with a gradle scope of {@code test} accordingly.
   *
   * <p>All children of {@code rootNode} named "attributes" which have a child named "attribute"
   * and an attribute named {@link #NAME_ATTRIBUTE} which have a value equal to {@code "test"}
   * become an attribute {@code test="true"} if they don't already have such an attribute.
   *
   * <p><i><b>Note 1:</b>
   * The eclipse plugin in Gradle version 5.3.1 do not mark test-artifacts properly.
   * So we do this here.
   * </i>
   *
   * <p><i><b>Note 2:</b>
   * The eclipse plugin in Gradle versions 5.6.4, 6.0 and 6.0.1 mark test-artifacts properly.
   * </i>
   *
   * @param rootNode
   *        XML-content to be improved
   */
  /* package */ void markTest(final Node rootNode) {
    // Example of a child to be improved
    // <classpathentry output="bin/test" kind="src" path="src/test/java">
    //   <attributes>
    //     <attribute name="gradle_scope" value="test"/>
    //     <attribute name="gradle_used_by_scope" value="test"/>
    //   </attributes>
    // </classpathentry>

    rootNode.children().stream()                              // loop over all children
        .filter(i  -> i instanceof Node)                      // better safe than sorry
        .filter(i  -> NAME_ITEM.equals(((Node)i).name()))     // with name "classpathentry"
        .filter(i  -> "test".equals(getGradleScope((Node)i))) // appropriate gradle scope
        .filter(i  -> hasNoAttributeTest((Node)i))            // without "test" information
        .forEach(i -> addAttribute((Node)i, "test"));         // add test="true" attribute
  } // end method */

  /**
   * Puts JRE entries in {@code rootNode} on module-path.
   *
   * <p>All children of {@code rootNode} with name {@link #NAME_CHILD} which are kind of "con"
   * and with a path containing {@link #NAME_JRE} are put on module-path.
   *
   * <p><i><b>Node:</b>
   * Currently Gradle's eclipse-plugin seems to be module agnostic. It seems safe to assume that
   * in case someone uses "gradle-modules-plugin" the corresponding code is modular and thus uses
   * a modular JRE. Thus the JRE is put on module-path.
   * </i>
   *
   * @param rootNode
   *        XML-content to be improved
   */
  /* package */ void putJreOnModulePath(final Node rootNode) {
    rootNode.children().stream()                          // loop over all children
        .filter(i  -> i instanceof Node)                  // better safe than sorry
        .filter(i  -> NAME_ITEM.equals(((Node)i).name())) // with name "classpathentry"
        .filter(i  -> isJre((Node)i))                     // indicating JRE
        .filter(i  -> hasNoAttributeModule((Node)i))      // without "module" information
        .forEach(i -> addAttribute((Node)i, "module"));           // put on module-path
  } // end method */

  /**
   * Retrieves gradle scope from given {@code item}.
   *
   * <p>The following actions are performed:
   * <ol>
   *   <li>Searches in the children of {@code item} for the first one named {@link #NAME_CHILD}.
   *   <li>If such a child doesn't exist an empty {@link String} is returned.
   *   <li>If such a child exists then in the children of that child for first one named
   *       {@link #NAME_GRAND} and an attribute with name {@link #NAME_ATTRIBUTE} is searched.
   *   <li>If such an attribute is present then its value is returned.
   *   <li>If such an attribute is absent  then an empty {@link String} is returned.
   * </ol>
   *
   * @param item
   *        investigated for a gradle scope
   *
   * @return value of attribute {@link #NAME_ATTRIBUTE} or
   *         an empty {@link String} if such an attribute is not present
   */
  /* package */ String getGradleScope(final Node item) {
    final String empty = ""; // value if gradle scope is absent

    // ... Note 1: In real usage (i.e. no test scenario) item has name "classpathentry".

    final Optional<Node> oChild = item.children().stream() // loop over all children
        .filter(c -> c instanceof Node)                    // better safe than sorry
        .filter(c -> NAME_CHILD.equals(((Node)c).name()))  // with name "attributes"
        .findFirst();                                      // first child named "attributes"

    if (oChild.isPresent()) {
      // ... child of type Node and name "attributes" is present
      //     => search there for grand-child with gradle scope attribute
      final Optional<Node> oGrand = getAttributeNamed(oChild.get(), NAME_ATTRIBUTE);

      if (oGrand.isPresent()) {
        // ... grandChild of type Node named "attribute" with attribute named "gradle_used_by_scope"
        //     => get its value (if there is one)
        final Node   grand = oGrand.get();
        final Object value = grand.attribute("value"); // returns null if value is absent

        return (null == value) ? empty : value.toString(); // avoid NullPointerException
      } // end if (oGrand present)
      // ... no appropriate grand-child present => return empty string
    } // end if (oChild present)
    // ... no appropriate child present => return empty string

    return empty;
  } // end method */

  /**
   * Retrieves first child of given {@code child} named "attribute" which has an attribute
   * named {@code name}.
   *
   * @param child
   *        {@link Node} for which the estimation is performed
   *
   * @param name
   *        of attribute searched for
   *
   * @return {@link Optional} for first child in {@code child} named "attribute" which has an
   *         attribute named {@code name}.
   *         If no such {@link Node} exists an empty {@link Optional} is returned.
   */
  /* package */ Optional<Node> getAttributeNamed(final Node child, final String name) {
    // ... Note 1: In real usage (i.e. no test scenario) node has name "attributes".

    return child.children().stream()                           // loop over all children
        .filter(g -> g instanceof Node)                        // better safe than sorry
        .filter(g -> NAME_GRAND.equals(((Node)g).name()))      // nodes with name "attribute"
        .filter(g -> name.equals(((Node)g).attribute("name"))) // nodes with appropriate attribute
        .findFirst();
  } // end method */

  /**
   * Estimates whether given {@code item} contains a value for a key named "module".
   *
   * @param item
   *        {@link Node} for which the estimation is performed
   *
   * @return false if {@code item} has at least one child named "attributes" and that child has
   *         at least one child named "attribute" containing an attribute named "module",
   *         true otherwise
   */
  /* package */ boolean hasNoAttributeModule(final Node item) {
    // ... Note 1: In real usage (i.e. no test scenario) item has name "classpathentry".

    return item.children().stream()                        // loop over all children
        .filter(c -> c instanceof Node)                    // better safe than sorry
        .filter(c -> NAME_CHILD.equals(((Node)c).name()))  // child named "attributes"
        .filter(c -> hasAttributeNamed((Node)c, "module")) // grand-child with attribute "module"
        .findFirst()
        .isEmpty();
  } // end method */

  /**
   * Estimates whether given {@code item} contains a value for a key named "test".
   *
   * @param item
   *        {@link Node} for which the estimation is performed
   *
   * @return false if {@code item} has at least one child named "attributes" and that child has
   *         at least one child named "attribute" containing an attribute named "test",
   *         true otherwise
   */
  /* package */ boolean hasNoAttributeTest(final Node item) {
    // ... Note 1: In real usage (i.e. no test scenario) item has name "classpathentry".

    return item.children().stream()                       // loop over all children
        .filter(c -> c instanceof Node)                   // better safe than sorry
        .filter(c -> NAME_CHILD.equals(((Node)c).name())) // child named "attributes"
        .filter(c -> hasAttributeNamed((Node)c, "test"))  // grand-child with attribute "test"
        .findFirst()
        .isEmpty();
  } // end method */

  /**
   * Estimates whether given {@code child} has a child named "attribute" and
   * that child has an attribute named {@code name}.
   *
   * @param child
   *        {@code Node} for which the estimation is performed
   *
   * @param name
   *        of attribute searched for
   *
   * @return true if {@code node} has at least one child named {@code attribute}
   *         containing an attribute named {@code name},
   *         false otherwise
   */
  /* package */ boolean hasAttributeNamed(final Node child, final String name) {
    // ... Note 1: In real usage (i.e. no test scenario) node has name "attributes".

    return getAttributeNamed(child, name).isPresent();
  } // end method */

  /**
   * Estimates whether given {@link Node} belongs to a JRE description.
   *
   * @param item
   *        a {@link Node} investigated whether it is of a certain kind
   *
   * @return true if given {@link Node} is kind of "con" and has an attribute "path" containing
   *         "JRE_CONTAINER",
   *         false otherwise
   */
  /* package */ boolean isJre(final Node item) {
    // ... Note 1: In real usage (i.e. no test scenario) node has name "classpathentry".

    final Object path = item.attribute("path"); // might return null

    return isKindOf(item, "con") && (null != path) && path.toString().contains(NAME_JRE);
  } // end method */

  /**
   * Estimates whether given {@link Node} is of certain kind.
   *
   * @param item
   *        a {@link Node} investigated whether it is of a certain kind
   *
   * @param kind
   *        type for which the given {@link Node} is checked
   *
   * @return true if the {@link Node} has attribute "kind" and the value of that attribute is
   *         equal to the given one in parameter {@code kind},
   *         false otherwise
   */
  /* package */ boolean isKindOf(final Node item, final String kind) {
    // ... Note 1: In real usage (i.e. no test scenario) node has name "classpathentry".

    final Object attr = item.attribute("kind"); // might return null

    return kind.equals(attr);
  } // end method */

  /**
   * Just a static nested class.
   *
   * @author <a href="mailto:alfred.65.fiedler@gmail.com">Alfred Fiedler</a>
   */
  private static class AddAttribute implements Runnable {
    /**
     * Item to which an attribute should be added.
     */
    private final transient Node insItem; // */

    /**
     * Map with attributes.
     */
    private final transient Map<String, String> insMap; // */

    /**
     * Comfort constructor.
     *
     * @param item
     *        where a new grand-children is added to
     *
     * @param map
     *        with attributes for grand-child
     */
    private AddAttribute(final Node item, final Map<String, String> map) {
      insItem = item;
      insMap  = map;
    } // end constructor

    @Override
    public void run() {
      // Note: Intentionally the return value of method ".appendNode(...) is ignored.
      insItem
          .appendNode(NAME_CHILD)          // add child with name "attributes" and
          .appendNode(NAME_GRAND, insMap); // grand-child with appropriate attributes
    } // end inner method
  } // end static nested class

  /**
   * Adds a grand-child {@link Node} named "attribute" and
   * {@code name="attributeName"} and
   * {@code value="true"}.
   *
   * <p>The implementation searches for the first child named "attributes" and
   * adds to that child a {@link Node} with name "attribute" and attributes
   * {@code attributeName="true"}.
   *
   * @param item
   *        where a new grand-children is added to
   *
   * @param attributeName
   *        name of attribute
   */
  /* package */ void addAttribute(final Node item, final String attributeName) {
    // ... Note 1: In real usage (i.e. no test scenario) item has name "classpathentry".

    final Map<String, String> map = new ConcurrentSkipListMap<>(); // NOPMD use concurrent map
    map.put("name",  attributeName);
    map.put("value", "true");

    // --- find first child named "attributes"
    item.children().stream()                              // loop over all children
        .filter(c -> c instanceof Node)                   // better safe than sorry
        .filter(c -> NAME_CHILD.equals(((Node)c).name())) // nodes with name "attributes"
        .findFirst()
        .ifPresentOrElse(
            // ... item has a child named "attributes"
            //     => add appropriate child to that
            // Note: Intentionally the return value of method ".appendNode(...) is ignored.
            c -> ((Node) c).appendNode(NAME_GRAND, map),

            // ... item has no child named "attributes"
            //     => add appropriate child to item
            new AddAttribute(item, map)
    ); // end ifPresentOrElse(...)
  } // end method */
} // end class