package org.javamodularity.moduleplugin.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import groovy.util.Node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.gradle.api.Project;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Class testing {@link ClasspathFile}.
 * 
 * <p><b>Note:</b> This class performs white-box-tests.
 * 
 * @author <a href="mailto:alfred.65.fiedler@gmail.com">Dr.-Ing. Alfred Fiedler</a>
 */
final class ClasspathFileTest {
  /**
   * Logger.
   *
  private static final Logger LOGGER = Logging.getLogger(ClasspathFileTest.class); // */
  
  /** Method executed before other tests. */
  @BeforeAll
  static void setUpBeforeClass() {
    // intentionally empty
  } // end method */
  
  /** Method executed after other tests. */
  @AfterAll
  static void tearDownAfterClass() {
    // intentionally empty
  } // end method */
  
  /** Method executed before each test. */
  @BeforeEach
  void setUp() {
    // intentionally empty
  } // end method */
  
  /** Method executed after each test. */
  @AfterEach
  void tearDown() {
    // intentionally empty
  } // end method */
  
  /**
   * Test method for {@link ClasspathFile#addAction(Project)}.
   */
  @Test
  @Disabled("don't know how to test")
  final void test_addAction__Project() {
    fail("Not yet implemented"); // TODO
  } // end method */
  
  /**
   * Test method for {@link ClasspathFile#hasAttributeNamed(Node, String)}.
   */
  @Test
  final void test_hasAttributeNamed__Node_String() {
    // Test strategy:
    // a. Node without children SHALL return false
    // b. Node with children not named "attribute" SHALL return false
    // c. Node with children named "attribute" but without proper attribute SHALL return false
    // d. Node with one or more children named "attribute" and proper attribute SHALL return true
    
    // --- setup a node used for testing
    final Node dut = new Node(null, "dut"); // device under test
    
    // --- setup a map with attributes for child-nodes
    final Map<String, String> mapItem = new LinkedHashMap<>();
    mapItem.put("name", "Alfred");
    mapItem.put("Name", "foo");
    
    // --- a. Node without children SHALL return false
    assertEquals(0, dut.children().size());
    assertFalse(ClasspathFile.hasAttributeNamed(dut, "foo"));

    // --- b. Node with children not named "attribute" SHALL return false
    dut.appendNode("Attribute", mapItem); // wrong capitalization
    dut.appendNode("attributes", mapItem); // extra characters
    assertFalse(ClasspathFile.hasAttributeNamed(dut, "foo"));
    
    // --- c. Node with children named "attribute" but without proper attribute SHALL return false
    // c.1 child "attribute" without attributes
    dut.appendNode(ClasspathFile.NAME_GRAND);
    assertFalse(ClasspathFile.hasAttributeNamed(dut, "foo"));
    
    // c.2 child "attribute" with attributes
    dut.appendNode(ClasspathFile.NAME_GRAND, mapItem);
    // Note: assertions for c.2 are combined with assertions in d, see below
    
    // --- d. Node with children named "attribute" and proper attribute SHALL return true
    // d.1 Just one proper child
    assertFalse(ClasspathFile.hasAttributeNamed(dut, "alfred"));  // wrong capitalization
    assertFalse(ClasspathFile.hasAttributeNamed(dut, " Alfred")); // extra prefix
    assertFalse(ClasspathFile.hasAttributeNamed(dut, "Alfreds")); // extra suffix
    assertTrue(ClasspathFile.hasAttributeNamed(dut, "Alfred"));
    
    // d.2 More than one proper child
    dut.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "Fiedler"));
    dut.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "bar"));
    assertTrue(ClasspathFile.hasAttributeNamed(dut, "Alfred"));  // match in first  proper child
    assertTrue(ClasspathFile.hasAttributeNamed(dut, "Fiedler")); // match in second proper child
    assertTrue(ClasspathFile.hasAttributeNamed(dut, "bar"));     // match in third  proper child
  } // end method */

  /**
   * Test method for {@link ClasspathFile#hasNoAttributeModule(Node)}.
   */
  @Test
  final void test_hasNoAttributeModule__Node() {
    // Test strategy:
    // --- a. "short" node, i.e. node with not enough information (returns always true)
    // --- b. node with just sufficient information
    // --- c. node triggering false by first  child
    // --- d. node triggering false by second child
    // --- e. node triggering false by third  child
    
    // --- a. "short" node, i.e. node with not enough information (returns always true)
    // a.1 empty node
    final Node nodeA = new Node(null, "a");
    assertTrue(ClasspathFile.hasNoAttributeModule(nodeA));
    
    // a.2 node with empty child
    final Node childA = nodeA.appendNode(ClasspathFile.NAME_CHILD);
    assertTrue(ClasspathFile.hasNoAttributeModule(nodeA));
    
    // a.3 node with empty grand-child
    Node grandA = childA.appendNode(ClasspathFile.NAME_GRAND);
    assertTrue(ClasspathFile.hasNoAttributeModule(nodeA));
    
    // a.4 node with non-empty grand-child, but inappropriate attributes
    childA.remove(grandA);
    grandA = childA.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "modul")); // not module
    // LOGGER.quiet("a.3: {}", nodeA);
    assertEquals(1, nodeA. children().size());
    assertEquals(1, childA.children().size());
    assertTrue(ClasspathFile.hasNoAttributeModule(nodeA));
    
    childA.remove(grandA);
    grandA = childA.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "mOdule")); // not module
    assertTrue(ClasspathFile.hasNoAttributeModule(nodeA));
    
    childA.remove(grandA);
    grandA = childA.appendNode(ClasspathFile.NAME_GRAND, Map.of("naMe", "module")); // not name
    assertTrue(ClasspathFile.hasNoAttributeModule(nodeA));
    
    // --- b. node with just sufficient information
    childA.remove(grandA);
    grandA = childA.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "module"));
    assertFalse(ClasspathFile.hasNoAttributeModule(nodeA));
    
    // --- c. node triggering false by first  child
    // two (slightly) different nodes, one returns true, the other false
    assertTrue(ClasspathFile.hasNoAttributeModule(hnam(1, Map.of("name", "modUle"))));
    assertFalse(ClasspathFile.hasNoAttributeModule(hnam(1, Map.of("name", "module"))));
    
    // --- d. node triggering false by second child
    assertTrue(ClasspathFile.hasNoAttributeModule(hnam(2, Map.of("name", "modUle"))));
    assertFalse(ClasspathFile.hasNoAttributeModule(hnam(2, Map.of("name", "module"))));
    
    // --- e. node triggering false by third  child
    assertTrue(ClasspathFile.hasNoAttributeModule(hnam(3, Map.of("name", "modUle"))));
    assertFalse(ClasspathFile.hasNoAttributeModule(hnam(3, Map.of("name", "module"))));
  } // end method */
  
  /**
   * Creates a node with three sub-nodes.
   * 
   * <p>If {@code pos} is not in range [1,3] then no sub-node is named "attributes":
   * <ol>
   *   <li>1st node has wrong capitalization
   *   <li>2nd node has trailing characters
   *   <li>3rd node has prefix
   * 
   * @param pos
   *        value from range [1, 3] indicating sub-node with proper name
   *  
   * @param map
   *        with attributes for grand-children
   * 
   * @return {@link Node} with three sub-nodes each having three sub-nodes
   */
  private Node hnam(
      final int                 pos,
      final Map<String, String> map
  ) {
    final Node result = new Node(null, "root");
    
    addGrand(result.appendNode(1 == pos ? ClasspathFile.NAME_CHILD :  "Attributes"),  pos, map);
    addGrand(result.appendNode(2 == pos ? ClasspathFile.NAME_CHILD :  "attributess"), pos, map);
    addGrand(result.appendNode(3 == pos ? ClasspathFile.NAME_CHILD : "pattributes"),  pos, map);
    
    return result;
  } // end method */
  
  /**
   * Adds three {@link Node}s to given node.
   * 
   * <p>If {@code pos} is not in range [1, 3] then no sub-node indicates attribute "module":
   * <ol>
   *   <li>1st node has trailing character ater "module"
   *   <li>2nd node has wrong capitalization
   *   <li>3rd node has wrong attribute name
   * </ol>
   * 
   * @param node
   *        where sub-nodes are added to
   * 
   * @param pos
   *        value from range [1,3] indicating which child should get attributes from {@code map}
   * 
   * @param map
   *        with attributes for child at position {@code pos}
   */
  private void addGrand(
      final Node                node,
      final int                 pos,
      final Map<String, String> map
  ) {
    final Map<String, String> map1 = Map.of("name", "modules"); // not module
    final Map<String, String> map2 = Map.of("name", "mOdule");  // not module
    final Map<String, String> map3 = Map.of("Name", "module");  // not name
    
    node.appendNode(ClasspathFile.NAME_GRAND, 1 == pos ? map : map1);
    node.appendNode(ClasspathFile.NAME_GRAND, 2 == pos ? map : map2);
    node.appendNode(ClasspathFile.NAME_GRAND, 3 == pos ? map : map3);
  } // end method */
  
  /**
   * Test method for {@link ClasspathFile#isJre(Node)}.
   */
  @Test
  final void test_isJre__Node() {
    // Test strategy:
    // --- a.  kind of "con"   and   path contains "JRE_CONTAINER"
    // --- b. !kind of "con"   but   path contains "JRE_CONTAINER"
    // --- c.  kind of "con"   but  !path contains "JRE_CONTAINER"
    
    // --- a.  kind of "con"   and   path contains "JRE_CONTAINER"
    assertTrue(ClasspathFile.isJre(new Node(null, "root", Map.of(
        "kind", "con",
        "path", ClasspathFile.NAME_JRE // minimalistic
    ))));
    assertTrue(ClasspathFile.isJre(new Node(null, "root", Map.of(
        "kind", "con",
        "path", "prefix" + ClasspathFile.NAME_JRE
    ))));
    assertTrue(ClasspathFile.isJre(new Node(null, "root", Map.of(
        "kind", "con",
        "path", ClasspathFile.NAME_JRE + "suffix"
    ))));
    
    // --- b. !kind of "con"   but   path contains "JRE_CONTAINER"
    assertFalse(ClasspathFile.isJre(new Node(null, "root", Map.of(
        // no attribute kind
        "path", ClasspathFile.NAME_JRE // minimalistic
    ))));
    Set.of(
        "kind",  // ok
        "kiNd",  // wrong capitalization
        "pkind", // prefix
        "kinds"  // suffix
    ).stream()
        .forEach(kind -> {
          assertEquals(
              "kind".equals(kind),
              ClasspathFile.isJre(new Node(null, "root", Map.of(
                  kind, "con",
                  "path", ClasspathFile.NAME_JRE
              )))
          );
        }); // end forEach(kind -> ...)
    Set.of(
        "con",  // ok
        "Con",  // wrong capitalization
        "pcon", // prefix
        "cons"  // suffix
    ).stream()
        .forEach(con -> {
          assertEquals(
              "con".equals(con),
              ClasspathFile.isJre(new Node(null, "root", Map.of(
                  "kind", con,
                  "path", ClasspathFile.NAME_JRE
              )))
          );
        }); // end forEach(con -> ...)
    
    // --- c.  kind of "con"   but  !path contains "JRE_CONTAINER"
    assertFalse(ClasspathFile.isJre(new Node(null, "root", Map.of(
        "kind", "con"
        // no path attribute
    ))));
    Set.of(
        "path",  // ok
        "pAth",  // wrong capitalization
        "ppath", // prefix
        "paths"  // suffix
    ).stream()
        .forEach(path -> {
          assertEquals(
              "path".equals(path),
              ClasspathFile.isJre(new Node(null, "root", Map.of(
                  "kind", "con",
                  path, ClasspathFile.NAME_JRE
              )))
          );
        }); // end forEach(path -> ...)
    Set.of(
        "JRE_CONTAINER", // ok
        "jRE_CONTAINER", // wrong capitalization
        "pJRE_CONTAINER", // prefix
        "JRE_CONTAINERs"  // suffix
    ).stream()
        .forEach(jre -> {
          assertEquals(
              jre.contains(ClasspathFile.NAME_JRE),
              ClasspathFile.isJre(new Node(null, "root", Map.of(
                  "kind", "con",
                  "path", jre
              )))
          );
        }); // end forEach(jre -> ...)
  } // end method */

  /**
   * Test method for {@link ClasspathFile#isKindOf(Node, String)}.
   */
  @Test
  final void test_isKindOf__Node_String() {
    // Test strategy:
    // --- a. node without attributes
    // --- b. node without attribute "kind"
    // --- c. node with attribute "kind" and different values for that attribute
    
    // --- a. node without attributes
    assertFalse(ClasspathFile.isKindOf(new Node(null, "a"), "con"));
    
    // --- b. node without attribute "kind"
    assertFalse(ClasspathFile.isKindOf(
        new Node(null, "a", Map.of("Kind", "con")), // wrong capitalization
        "con"
    ));
    assertFalse(ClasspathFile.isKindOf(
        new Node(null, "a", Map.of("kinda", "con")), // suffix
        "con"
    ));
    assertFalse(ClasspathFile.isKindOf(
        new Node(null, "a", Map.of("akind", "con")), // prefix
        "con"
    ));
    
    // --- c. node with attribute "kind" and different values for that attribute
    final Set<String> variants = Set.of(
        "con",
        "lib",
        "foo",
        "bar"
    );
    
    final Map<String, String> attributes = new LinkedHashMap<>();
    variants.stream().forEach(i -> attributes.put(i, i));
    
    variants.stream() // loop over all variants
        .forEach(kind -> {
          // node with just one attribute
          assertTrue(ClasspathFile.isKindOf(new Node(null, "root", Map.of("kind", kind)), kind));
          
          // node with lots of attributes, but none fitting
          final Node dut = new Node(null, "root" + kind, attributes);
          variants.stream()
              .forEach(i -> assertFalse(ClasspathFile.isKindOf(dut, i)));
        }); // end forEach(kind -> ...)
  } // end method */

  /**
   * Test method for {@link ClasspathFile#moveToModulePath(Node)}.
   */
  @Test
  final void test_moveToModulePath__Node() {
    // Test strategy:
    // --- a. empty node
    // --- b. node with children none named "attributes"
    // --- c. node with several children named "attributes"
    // --- d. node already moved
    
    Node dut;
    
    // --- a. empty node
    dut = new Node(null, "rootA");
    ClasspathFile.moveToModulePath(dut);
    assertEquals(
        "rootA[attributes={}; value=["
        +   "attributes[attributes={}; value=["
        +     "attribute[attributes={name=module, value=true}; value=[]]"
        +   "]]"
        + "]]",
        dut.toString()
    );
    
    // --- b. node with children none named "attributes"
    dut = new Node(null, "rootB");
    dut.appendNode("foo");
    dut.appendNode("bar");
    ClasspathFile.moveToModulePath(dut);
    assertEquals(
        "rootB[attributes={}; value=["
        +   "foo[attributes={}; value=[]], "
        +   "bar[attributes={}; value=[]], "
        +   "attributes[attributes={}; value=["
        +     "attribute[attributes={name=module, value=true}; value=[]]"
        +   "]]"
        + "]]",
        dut.toString()
    );
    
    // --- c. node with several children named "attributes"
    // c.1 one child named "attributes"
    dut = new Node(null, "rootC1");
    dut.appendNode(ClasspathFile.NAME_CHILD);
    ClasspathFile.moveToModulePath(dut);
    assertEquals(
        "rootC1[attributes={}; value=["
        +   "attributes[attributes={}; value=["
        +     "attribute[attributes={name=module, value=true}; value=[]]"
        +   "]]"
        + "]]",
        dut.toString()
    );
    
    // c.2 two children named "attributes" but one other node before
    dut = new Node(null, "rootC2");
    dut.appendNode("foo");
    Node child = dut.appendNode(ClasspathFile.NAME_CHILD);
    child.appendNode("bar");
    dut.appendNode(ClasspathFile.NAME_CHILD);
    ClasspathFile.moveToModulePath(dut);
    assertEquals(
        "rootC2[attributes={}; value=["
        +   "foo[attributes={}; value=[]], "
        +   "attributes[attributes={}; value=["
        +     "bar[attributes={}; value=[]], "
        +     "attribute[attributes={name=module, value=true}; value=[]]"
        +   "]], "
        +   "attributes[attributes={}; value=[]]"
        + "]]",
        dut.toString()
    );
    
    // c.3 three children named "attributes" some other nodes around
    dut = new Node(null, "rootC3");
    dut.appendNode("foo");
    child = dut.appendNode(ClasspathFile.NAME_CHILD, Map.of("ping", "pong"));
    child.appendNode("bar");
    child = dut.appendNode(ClasspathFile.NAME_CHILD);
    child.appendNode("bar2");
    dut.appendNode("foo2");
    ClasspathFile.moveToModulePath(dut);
    assertEquals(
        "rootC3[attributes={}; value=["
        +   "foo[attributes={}; value=[]], "
        +   "attributes[attributes={ping=pong}; value=["
        +     "bar[attributes={}; value=[]], "
        +     "attribute[attributes={name=module, value=true}; value=[]]"
        +   "]], "
        +   "attributes[attributes={}; value=["
        +     "bar2[attributes={}; value=[]]"
        +   "]], "
        +   "foo2[attributes={}; value=[]]"
        + "]]",
        dut.toString()
    );
    
    // --- d. node already moved
    // d.1 move information in first child with name "attributes"
    ClasspathFile.moveToModulePath(dut);
    assertEquals(
        "rootC3[attributes={}; value=["
        +   "foo[attributes={}; value=[]], "
        +   "attributes[attributes={ping=pong}; value=["
        +     "bar[attributes={}; value=[]], "
        +     "attribute[attributes={name=module, value=true}; value=[]], "
        +     "attribute[attributes={name=module, value=true}; value=[]]"
        +   "]], "
        +   "attributes[attributes={}; value=["
        +     "bar2[attributes={}; value=[]]"
        +   "]], "
        +   "foo2[attributes={}; value=[]]"
        + "]]",
        dut.toString()
    );
    
    // d.2 move information in 2nd child with name "attributes"
    final Map<String, String> mapD2 = new LinkedHashMap<>();
    mapD2.put("name", "module");
    mapD2.put("value", "true");
    
    dut = new Node(null, "rootD2");
    dut.appendNode("foo");
    child = dut.appendNode(ClasspathFile.NAME_CHILD, Map.of("ping", "pong"));
    child.appendNode("bar");
    child = dut.appendNode(ClasspathFile.NAME_CHILD);
    child.appendNode("bar2");
    child.appendNode(ClasspathFile.NAME_GRAND, mapD2);
    dut.appendNode("foo2");
    ClasspathFile.moveToModulePath(dut);
    assertEquals(
        "rootD2[attributes={}; value=["
        +   "foo[attributes={}; value=[]], "
        +   "attributes[attributes={ping=pong}; value=["
        +     "bar[attributes={}; value=[]], "
        +     "attribute[attributes={name=module, value=true}; value=[]]"
        +   "]], "
        +   "attributes[attributes={}; value=["
        +     "bar2[attributes={}; value=[]], "
        +     "attribute[attributes={name=module, value=true}; value=[]]"
        +   "]], "
        +   "foo2[attributes={}; value=[]]"
        + "]]",
        dut.toString()
    );
  } // end method */

  /**
   * Test method for {@link org.javamodularity.moduleplugin.tasks.ClasspathFile#improveEclipseClasspathFile(groovy.util.Node)}.
   */
  @Test
  final void testImproveEclipseClasspathFile() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link org.javamodularity.moduleplugin.tasks.ClasspathFile#putJreOnModulePath(java.util.List)}.
   */
  @Test
  final void testPutJreOnModulePath() {
    fail("Not yet implemented"); // TODO
  }
} // end class