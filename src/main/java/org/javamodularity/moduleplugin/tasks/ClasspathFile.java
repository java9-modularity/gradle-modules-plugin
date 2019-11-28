package org.javamodularity.moduleplugin.tasks;

import groovy.util.Node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.XmlProvider;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.plugins.ide.api.XmlFileContentMerger;
import org.gradle.plugins.ide.eclipse.GenerateEclipseClasspath;
import org.gradle.plugins.ide.eclipse.model.EclipseClasspath;
import org.javamodularity.moduleplugin.JavaProjectHelper;
import org.javamodularity.moduleplugin.extensions.ClasspathFileExtension;

/**
 * This class provides functionality to improve the content of {@code .classpath} file created
 * by the {@code eclipse} plugin.
 * 
 * @author <a href="mailto:alfred.65.fiedler@gmail.com">Alfred Fiedler</a>
 */
//@SuppressWarnings("unchecked") // keep Eclipse code-checker happy
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
   * The project.
   */
  private final Project insProject; // */
  
  /**
   * Extension controlling whether {@code .classpath} file is improved or not.
   */
  private final ClasspathFileExtension insExtension; // */
  
  /**
   * @param project
   *        this instance is related to
   */
  public ClasspathFile(final Project project) {
    insProject = project;
    insExtension = insProject.getExtensions().create(
        "classpathFileExtension", ClasspathFileExtension.class, insProject
    );
  } // end constructor */
  
  /**
   * Configures appropriate action if project has task "eclipseClasspath".
   */
  public void configure() {
    insProject.afterEvaluate(p -> {
      final boolean flag = insExtension.getImproveClasspathFile().get();
      LOGGER.debug("configure, extension.value   : {}", flag);
      
      if (flag) {
        // ... flag indicates that .classpath file should be improved
        LOGGER.debug("gradle-modules-plugin configures improvement of .classpath-file");
        new JavaProjectHelper(insProject)
          .findTask("eclipseClasspath", Task.class)
          .ifPresent(this::configure);
      } else {
        // ... flag indicates that .classpath file shouldn't be improved
        LOGGER.debug(".classpath-file isn't changed by gradle-modules-plugin");
      } // end if
    });
  } // end method */
  
  /**
   * Configurations.
   * 
   * @param task
   *        responsible for generating {@code .classpath} file
   */
  private void configure(final Task task) {
    // LOGGER.quiet("configure, task: {}", task.getClass());
    
    // --- add functionality for enhancing the .classpath content
    // Note: For more information see the manual for the eclipse-plugin.
    final EclipseClasspath eclipseClasspath = ((GenerateEclipseClasspath) task).getClasspath();
    eclipseClasspath.file(new Action<XmlFileContentMerger>() {
      @Override
      public void execute(final XmlFileContentMerger xmlMerger) {
        xmlMerger.withXml(new Action<XmlProvider>() {
          @Override
          public void execute(final XmlProvider xmlProvider) {
            final Node rootNode = xmlProvider.asNode();
            LOGGER.debug("addAction: rootNode.before improving:{}", rootNode);
            improveEclipseClasspathFile(rootNode);
            LOGGER.debug("addAction: rootNode.after  improving:{}", rootNode);
          } // end inner method
        }); // end inner class Action<XmlProvider)
      } // end inner method
    }); // end inner class Action<XmlFileContentManager)
  } // end method */
  
  /**
   * Improve ".classpath"-file.
   * 
   * <p>This method modifies the given content of a ".classpath"-file in the following ways:
   * <ol>
   *   <li>Each "classpathentry" with a gradle_used_by_scope of test
   *       gets an additional attribute test = "true".
   *   <li>Each "classpathentry" of kind = "con" with a path containing "JRE_CONTAINER"
   *       is moved to the module-path.
   *   <li>Each "classpathentry" of kind = "lib" with a gradle_used_by_scope of "main"
   *       is moved to the module-path.
   * </ol>
   * 
   * @param rootNode
   *        XML-content to be improved
   */
  /* package */ void improveEclipseClasspathFile(final Node rootNode) {
    putJreOnModulePath(rootNode);
    markTest(rootNode);
    //markMain(rootNode);
    
    // #############################################################################################
    // Note 20: The eclipse-plugin in Gradle puts everything on the classpath and nothing on the
    //          module-path. Its seems appropriate to adjust things such that non-test dependencies
    //          are tagged such that they appear on the module-path. This works at least with the
    //          following versions:
    //          Gradle 5.6.2, Eclipse 2019-09
    
    // #############################################################################################
    // Note 30: Strategy for improving is as follows:
    //          a. check that we got an XML-node.
    //          b. loop over all children in given XML-node
    //          c. if node's attribute "kind" has value "lib" then check how gradle treats it.
    //             If gradle's scope contains main then add a node to "attribute" with content:
    //             <attribute name="module" value="true"/>
    
    // Excerpt from an entry which should be improved:
    // <classpathentry sourcepath="D:/.p/..." kind="lib" path="D:/.p/...">
    //   <attributes>
    //     <attribute name="gradle_used_by_scope" value="main,test"/>
    //   </attributes>
    // </classpathentry>
    
    // System.out.printf("improve before:%n%s%n", object);
    
    /*
    final Map<String, String> flagTest = new LinkedHashMap<>();
    flagTest.put("name",  "test");
    flagTest.put("value", "true");
    
    // --- loop over all items in rootNode and collect those of type Node with name "classpathentry"
    final List<Node> classpathEntries = (List<Node>) rootNode.children().stream()
        .filter(i -> i instanceof Node) // better safe than sorry
        .filter(i -> NAME_ITEM.equals(((Node)i).name())) // filter nodes with name "classpathentry"
        .collect(Collectors.toList());
    
    
    
    
    
        .filter(i -> isKindOfLib(i)) // filter nodes which are kind of "lib"
        .forEach(item -> {
          // ... item has type Node and name "classpathentry"
          // ... item is a kind of "lib"
          
          // --- loop over all children of item and investigate those with name "attributes"
          ((Node) item).children().stream()
              .filter(j -> j instanceof Node) // better safe than sorry
              .filter(j -> NAME_CHILD.equals(((Node)j).name())) // nodes with name "attributes"
              .filter(j -> shouldBeOnModulePath(j)) // child should be on module-path
              .forEach(j -> {
                // ... item has type Node and name "classpathentry"
                // ... item is a kind of "lib"
                // ... child of item has type Node and name "attributes"
                // ... grandChild of item indicates that it should be on module-path, but isn't yet
                ((Node) j).appendNode(NAME_GRAND, flagModule); // move to module-path
              }); // end forEach(j -> ...)
        }); // end forEach(i -> ...)
        // */
  } // end method */
  
  /**
   * Marks everything with a {@code gradle_used_by_scope} of {@code test} accordingly.
   * 
   * <p>All children of {@code rootNode} with name {@link #NAME_CHILD} which have a child with
   * name {@link #NAME_GRAND} and an attribute with name {@link #NAME_ATTRIBUTE} which has a value
   * equal to {@code "test"} become an attribute "test" and value "true" if they don't already have
   * such an attribute.
   * 
   * <p><i><b>Note 1:</b>
   * The eclipse plugin in Gradle version 5.3.1 do not mark test-artifacts properly.
   * So we do this here.
   * </i>
   * 
   * <p><i><b>Note 2:</b>
   * The eclipse plugin in Gradle versions 5.6.4 mark test-artifacts properly.
   * </i>
   * 
   * @param rootNode
   *        XML-content to be improved
   */
  /* package */ void markTest(final Node rootNode) {
    // Example of node to be improved
    // <classpathentry output="bin/test" kind="src" path="src/test/java">
    //   <attributes>
    //     <attribute name="gradle_scope" value="test"/>
    //     <attribute name="gradle_used_by_scope" value="test"/>
    //   </attributes>
    // </classpathentry>
    
    rootNode.children().stream()                          // loop over all children
        .filter(i  -> i instanceof Node)                  // better safe than sorry
        .filter(i  -> NAME_ITEM.equals(((Node)i).name())) // with name "classpathentry"
        .forEach(i -> checkGradleScopeTest((Node)i));     // add test="true" attribute
  } // end method */
  
  /**
   * Puts JRE entries in {@code rootNode} on module-path.
   * 
   * <p>All children of {@code rootNode} with name {@link #NAME_CHILD} which are kind of "con"
   * and with a path containing {@link #NAME_JRE} are put on module-path.
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
        .forEach(i -> moveToModulePath((Node)i));         // put on module-path
  } // end method */

  /**
   * Estimates whether given {@code node} contains a value for a key named "module".
   * 
   * @param item
   *        {@code Node} for which the estimation is performed
   *        
   * @return false if {@code node} has at least one child with name "attributes" and that child has
   *         at least one child with name "attribute" containing an attribute named "module",
   *         true otherwise
   */
  /* package */ boolean hasNoAttributeModule(final Node item) {
    // ... Note 1: In real usage (i.e. no test scenario) item has name "classpathentry".
    
    return item.children().stream()                       // loop over all children of node
        .filter(c -> c instanceof Node)                   // better safe than sorry
        .filter(c -> NAME_CHILD.equals(((Node)c).name())) // nodes with name "attributes"
        .filter(c -> hasAttributeNamed((Node)c, "module"))
        .findFirst()
        .isEmpty();
  } // end method */
  
  /**
   * Retrieves first child of given {@code child} named "attribute" which has an attribute
   * named {@code name}.
   *  
   * @param child
   *        {@code Node} for which the estimation is performed
   * 
   * @param name
   *        of attribute searched for
   * 
   * @return {@link Optional} for first child in {@code child} named "attribute" which has an
   *         attribute named {@code name}
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
   * Retrieves gradle scope from given {@code node}.
   * 
   * <p>The following actions are performed:
   * <ol>
   *   <li>Searches in the children of {@code node} for first one with name "attribute" and an
   *       attribute with name {@link #NAME_ATTRIBUTE}.
   *   <li>If such an attribute is present then its value is returned.
   *   <li>If such an attribute is absent  then an empty {@link String} is returned.
   * </ol>
   * 
   * @param node
   *        investigated for a child with name "attribute" and an attribute named
   *        {@link #NAME_ATTRIBUTE}
   * 
   * @return value of attribute {@link #NAME_ATTRIBUTE} or
   *         an empty {@link String} if such an attribute is not present
   */
  /* package */ String getGradleScope(final Node node) {
    // ... Note 1: In real usage (i.e. no test scenario) node has name "attributes".
    
    final Optional<Node> oChild = getAttributeNamed(node, NAME_ATTRIBUTE);
    
    if (oChild.isPresent()) {
      // ... child with type Node and name "attribute" with attribute named "gradle_used_by_scope"
      //     => get its value
      final Node   child = oChild.get();
      final Object value = child.attribute("value"); // might return null if value is absent
      
      return (null == value) ? "" : value.toString(); // avoid NullPointerException
    } // end if
    // ... no appropriate child present => return empty string
    
    return "";
  } // end method */

  /**
   * Estimates whether given {@code child} has a child named {@code "attribute"} and
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
   * Improves nodes with a gradle scope of "test".
   * 
   * <p>The following actions are performed:
   * <ol>
   *   <li>Searches in given {@code node} for first child of
   *       type {@link Node} named {@link #NAME_CHILD}.
   *   <li>If that child has a gradle cope of "test" but no child named {@link #NAME_GRAND} with
   *       an attribute named {@code test} then such child is added with {@code test="true"}.
   * </ol>
   * 
   * @param node
   *        a {@link Node} investigated whether it has a certain scope
   * 
   */
  /* package */ void checkGradleScopeTest(final Node node) {
    // ... Note 1: In real usage (i.e. no test scenario) node has name "classpathentry".
    
    node.children().stream() // loop over all children
        .filter(i -> i instanceof Node)                   // better safe than sorry
        .filter(i -> NAME_CHILD.equals(((Node)i).name())) // with name "attributes"
        .findFirst()                                      // first child named "attributes"
        .ifPresent(c -> {
          final Node child = (Node)c;
          if (
              "test".equals(getGradleScope(child)) // appropriate gradle scope
              && !hasAttributeNamed(child, "test") // actually no "test"-attribute
          ) {
            // ... child with name "attributes" is present
            // ... child has a  child named "attribute" with a gradle scope equal to "test"
            // ... child has no child named "attribute" with a "test"-attribute
            
            // --- add a child named "attribute" with test="true"
            final Map<String, String> map = new LinkedHashMap<>();
            map.put("name",  "test");
            map.put("value", "true");
            child.appendNode(NAME_GRAND, map);
          } // end if
        });
  } // end method */
  
  /**
   * Estimates whether given {@link Node} belongs to a JRE description.
   * 
   * @param node
   *        a {@link Node} investigated whether it is of a certain kind
   * 
   * @return true if given {@link Node} is kind of "con" and has an attribute "path" containing
   *         "JRE_CONTAINER",
   *         false otherwise
   */
  /* package */ boolean isJre(final Node node) {
    // ... Note 1: In real usage (i.e. no test scenario) node has name "classpathentry".
    
    final Object path = node.attribute("path"); // might return null
    
    return isKindOf(node, "con") && (null != path) && path.toString().contains(NAME_JRE);
  } // end method */

  /**
   * Estimates whether given {@link Node} is of certain kind.
   * 
   * @param node
   *        a {@link Node} investigated whether it is of a certain kind
   * 
   * @param kind
   *        type for which the given {@link Node} is checked
   *        
   * @return true if the {@link Node} has attribute "kind" and the value of that attribute is
   *         equal to the given one in parameter {@code kind},
   *         false otherwise
   */
  /* package */ boolean isKindOf(final Node node, final String kind) {
    final Object attr = node.attribute("kind"); // might return null
    
    return kind.equals(attr);
  } // end method */

  /**
   * Adds an attribute such that given {@code node} appears on the module path.
   * 
   * <p>The implementation searches for the first child with name "attributes" and adds to that
   * child a {@link Node} with name "attribute" and attributes
   * {@code name="module"} and
   * {@code value="true"}.
   *  
   * @param node
   *        which should appear on the module path
   */
  /* package */ void moveToModulePath(final Node node) {
    final Map<String, String> flagModule = new LinkedHashMap<>();
    flagModule.put("name",  "module");
    flagModule.put("value", "true");
  
    // --- find first child named "attributes"
    node.children().stream()                              // loop over all children
        .filter(c -> c instanceof Node)                   // better safe than sorry
        .filter(n -> NAME_CHILD.equals(((Node)n).name())) // nodes with name "attributes"
        .findFirst()
        .ifPresentOrElse(
            // ... node has a child named "attributes"
            //     => add appropriate child to that
            n -> ((Node) n).appendNode(NAME_GRAND, flagModule),
            
            // ... node has no child named "attributes"
            //     => add appropriate child to node
            new Runnable() {
              @Override
              public void run() {
                node
                    .appendNode(NAME_CHILD)               // add child with name "attributes" and
                    .appendNode(NAME_GRAND, flagModule);  // grand-child with appropriate attributes
              } // end inner method
            } // end inner class Runnable
        ); // end ifPresentOrElse(...)
  } // end method */

  /**
   * Estimates whether the given {@link Node} indicates that is should be on the module-path.
   *  
   * @param object
   *        a {@link Node}
   * 
   * @return true if a sub-node of given one indicates a gradle scope "main" but didn't indicate
   *         that it should be on module-path,
   *         false otherwise
   */
  private static boolean shouldBeOnModulePath(final Object object) {
    // ... assertion object is of type Node
    // ... assertion object has name "attributes"
    final Node child = (Node) object;
    
    // check whether children of child contain a gradle scope and
    // if so whether that contains main
    // if so whether they already contain a module-path entry
    // if not put that entry on module-path
    
    final boolean isGradleScopeMain = child.children().stream()
        .filter(k -> k instanceof Node) // better safe than sorry
        .filter(k -> NAME_GRAND.equals(((Node)k).name())) // nodes with name "attribute"
        .filter(k -> isGradleScopeMain(k)) // nodes with gradle scope of main
        .findFirst() // one grandChild per child is enough to trigger movement
        .isPresent();
    
    if (isGradleScopeMain) {
      // ... child has a gradle scope of "main"
      //     => check whether it is already on the module-path
      
      final boolean isAlreadyMoved = child.children().stream()
          .filter(k -> k instanceof Node) // better safe than sorry
          .filter(k -> NAME_GRAND.equals(((Node)k).name())) // nodes with name "attribute"
          .filter(k -> isAlreadyMoved(k)) // nodes indicating movement
          .findFirst() // one grandChild per child is enough to trigger non-movement
          .isPresent();
      
      return !isAlreadyMoved;
    } // end if
    
    return false;
  } // end method */
  
  /**
   * Estimates whether given object indicates a gradle scope of "main".
   * 
   * @param object
   *        a {@link Node}
   *        
   * @return true if given {@link Node} has a sub-node with name "attributes" and that sub-node
   *         has an attribute "gradle_used_by_scope" and that attribute contains "main",
   *         false otherwise
   */
  private static boolean isAlreadyMoved(final Object object) {
    // ... assertion object is of type Node
    // ... assertion has name "attribute"
    
    final Node grandChild = (Node) object;
    final Object name  = grandChild.attribute("name");  // might be null
    final Object value = grandChild.attribute("value"); // might be null
    return "module".equals(name) && "true".equals(value);
  } // end method */
  
  /**
   * Estimates whether given object indicates being an module-path.
   * 
   * @param object
   *        a {@link Node}
   *        
   * @return true if given {@link Node} has a sub-node with name "attributes" and that sub-node
   *         has an attribute "module" and that attribute contains "true",
   *         false otherwise
   */
  private static boolean isGradleScopeMain(final Object object) {
    // ... assertion object is of type Node
    // ... assertion has name "attribute"
    
    final Node grandChild = (Node) object;
    final Object name  = grandChild.attribute("name");  // might be null
    final Object scope = grandChild.attribute("value"); // might be null
    return NAME_ATTRIBUTE.equals(name)
        && (null != scope) // avoid NullPointerException
        && scope.toString().contains("main"); // scope isn't null here
  } // end method */
} // end class