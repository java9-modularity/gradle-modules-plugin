package org.javamodularity.moduleplugin.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import groovy.util.Node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
  
  /**
   * Device under test.
   */
  private ClasspathFile insDut; // */
  
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
    insDut = new ClasspathFile(ProjectBuilder.builder().build());
  } // end method */
  
  /** Method executed after each test. */
  @AfterEach
  void tearDown() {
    // intentionally empty
  } // end method */
  
  /**
   * Test method for {@link ClasspathFile#checkGradleScopeTest(Node)}.
   */
  @Test
  void test_checkGradleScopeTest__Node() {
    // Test strategy:
    // --- a. item without children
    // --- b. item without proper children
    // --- c. item with "attributes" but no grand-children
    // --- d. item -> "attributes" -> "attribute" but no attributes
    // --- e. item -> "attributes" -> "attribute" but non gradle scope attributes
    // --- f. item -> "attributes" -> "attribute" but gradle scope != test
    // --- g. item -> "attributes" -> "attribute" and gradle scope == test, no  test-attribute
    // --- h. item -> "attributes" -> "attribute" and gradle scope == test plus test-attribute
    // --- i. only first child named "attributes" count
    
    Node dut;
    
    // --- a. item without children
    dut = new Node(null, "item");
    insDut.checkGradleScopeTest(dut);
    assertEquals(
        "item[attributes={}; value=[]]",
        dut.toString()
    );
    
    // --- b. item without proper children
    dut.appendNode("foo.bar");
    insDut.checkGradleScopeTest(dut);
    assertEquals(
        "item[attributes={}; value=["
        + "foo.bar[attributes={}; value=[]]"
        + "]]",
        dut.toString()
    );
    
    // --- c. item with "attributes" but no grand-children
    Node child = dut.appendNode(ClasspathFile.NAME_CHILD);
    insDut.checkGradleScopeTest(dut);
    assertEquals(
        "item[attributes={}; value=["
        + "foo.bar[attributes={}; value=[]], "
        + "attributes[attributes={}; value=[]]"
        + "]]",
        dut.toString()
    );
    
    // --- d. item -> "attributes" -> "attribute" but no attributes
    Node grand = child.appendNode(ClasspathFile.NAME_GRAND);
    insDut.checkGradleScopeTest(dut);
    assertEquals(
        "item[attributes={}; value=["
        + "foo.bar[attributes={}; value=[]], "
        + "attributes[attributes={}; value=["
        +   "attribute[attributes={}; value=[]]"
        + "]]"
        + "]]",
        dut.toString()
    );
    child.remove(grand);
    
    // --- e. item -> "attributes" -> "attribute" but non gradle scope attributes
    final Map<String, String> mapE = new LinkedHashMap<>();
    mapE.put("name", "gradle_used_by_Scope");
    mapE.put("value", "test");
    grand = child.appendNode(ClasspathFile.NAME_GRAND, mapE);
    insDut.checkGradleScopeTest(dut);
    assertEquals(
        "item[attributes={}; value=["
        + "foo.bar[attributes={}; value=[]], "
        + "attributes[attributes={}; value=["
        +   "attribute[attributes={name=gradle_used_by_Scope, value=test}; value=[]]"
        + "]]"
        + "]]",
        dut.toString()
    );
    child.remove(grand);
    
    // --- f. item -> "attributes" -> "attribute" but gradle scope != test
    // f.1 gradle scope without value
    final Map<String, String> mapF = new LinkedHashMap<>();
    mapF.put("name", "gradle_used_by_scope");
    grand = child.appendNode(ClasspathFile.NAME_GRAND, mapF);
    insDut.checkGradleScopeTest(dut);
    assertEquals(
        "item[attributes={}; value=["
        + "foo.bar[attributes={}; value=[]], "
        + "attributes[attributes={}; value=["
        +   "attribute[attributes={name=gradle_used_by_scope}; value=[]]"
        + "]]"
        + "]]",
        dut.toString()
    );
    // f.2 gradle scope != test
    mapF.put("value", "main,test");
    insDut.checkGradleScopeTest(dut);
    assertEquals(
        "item[attributes={}; value=["
        + "foo.bar[attributes={}; value=[]], "
        + "attributes[attributes={}; value=["
        +   "attribute[attributes={name=gradle_used_by_scope, value=main,test}; value=[]]"
        + "]]"
        + "]]",
        dut.toString()
    );
    child.remove(grand);
    
    // --- g. item -> "attributes" -> "attribute", gradle scope == test, no  test-attribute
    final String expectedG = "item[attributes={}; value=["
        + "foo.bar[attributes={}; value=[]], "
        + "attributes[attributes={}; value=["
        +   "attribute[attributes={name=gradle_used_by_scope, value=test}; value=[]], "
        +   "attribute[attributes={name=test, value=true}; value=[]]"
        + "]]"
        + "]]";
    final Map<String, String> mapG = new LinkedHashMap<>();
    mapG.put("name", "gradle_used_by_scope");
    mapG.put("value", "test");
    grand = child.appendNode(ClasspathFile.NAME_GRAND, mapG);
    insDut.checkGradleScopeTest(dut);        // call method under test once
    assertEquals(expectedG, dut.toString()); // check result
    insDut.checkGradleScopeTest(dut);        // call method under test again
    assertEquals(expectedG, dut.toString()); // node under test shouldn't change
    child.remove(grand);
    
    // --- h. item -> "attributes" -> "attribute" but gradle scope == test plus test-attribute
    // it is expected that method under test doesn't change node
    dut = new Node(null, "item");
    child = dut.appendNode(ClasspathFile.NAME_CHILD);
    child.append(grand);
    child.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "test"));
    insDut.checkGradleScopeTest(dut);
    assertEquals(
        "item[attributes={}; value=["
        + "attributes[attributes={}; value=["
        +   "attribute[attributes={name=gradle_used_by_scope, value=test}; value=[]], "
        +   "attribute[attributes={name=test}; value=[]]"
        + "]]"
        + "]]",
        dut.toString()
    );
    
    // --- i. only first child named "attributes" count
    // i.1 proper scope in 1st child
    dut = new Node(null, "item");
    final Node properScope = dut.appendNode(ClasspathFile.NAME_CHILD);
    properScope.appendNode(ClasspathFile.NAME_GRAND, mapG);
    insDut.checkGradleScopeTest(dut);
    assertEquals(
        "item[attributes={}; value=["
        + "attributes[attributes={}; value=["
        +   "attribute[attributes={name=gradle_used_by_scope, value=test}; value=[]], "
        +   "attribute[attributes={name=test, value=true}; value=[]]"
        + "]]"
        + "]]",
        dut.toString()
    );
    
    // i.2 another improper child named "attributes" before proper child
    dut.remove(properScope);
    dut.appendNode(ClasspathFile.NAME_CHILD);
    dut.appendNode(ClasspathFile.NAME_CHILD).appendNode(ClasspathFile.NAME_GRAND, mapG);
    insDut.checkGradleScopeTest(dut);
    assertEquals(
        "item[attributes={}; value=["
        + "attributes[attributes={}; value=[]], "
        + "attributes[attributes={}; value=["
        +   "attribute[attributes={name=gradle_used_by_scope, value=test}; value=[]]"
        + "]]"
        + "]]",
        dut.toString()
    );
  } // end method */

  /**
   * Test method for {@link ClasspathFile#getAttributeNamed(Node, String)}.
   */
  @Test
  void test_getAttributeNamed__Node_String() {
    // Test strategy:
    // a. Node without children SHALL return empty
    // b. Node with children not named "attribute" SHALL return empty
    // c. Node with children named "attribute" but without proper attribute SHALL return empty
    // d. Node with one or more children named "attribute" and proper attribute SHALL return node
    
    // --- setup a node used for testing
    final Node dut = new Node(null, "dut"); // device under test
    
    // --- setup a map with attributes for child-nodes
    final Map<String, String> mapItem = new LinkedHashMap<>();
    mapItem.put("name", "Alfred");
    mapItem.put("Name", "foo");
    
    // --- a. Node without children SHALL return empty
    assertEquals(0, dut.children().size());
    assertEquals(false, insDut.getAttributeNamed(dut, "foo").isPresent());

    // --- b. Node with children not named "attribute" SHALL return empty
    dut.appendNode("Attribute", mapItem); // wrong capitalization
    dut.appendNode("attributes", mapItem); // extra characters
    assertEquals(false, insDut.getAttributeNamed(dut, "foo").isPresent());
    
    // --- c. Node with children named "attribute" but without proper attribute SHALL return empty
    // c.1 child "attribute" without attributes
    dut.appendNode(ClasspathFile.NAME_GRAND);
    assertEquals(false, insDut.getAttributeNamed(dut, "foo").isPresent());
    
    // c.2 child "attribute" with attributes
    final Node node1 = dut.appendNode(ClasspathFile.NAME_GRAND, mapItem);
    // Note: assertions for c.2 are combined with assertions in d, see below
    
    // --- d. Node with children named "attribute" and proper attribute SHALL return node
    // d.1 Just one proper child
    assertFalse(insDut.getAttributeNamed(dut, "alfred").isPresent());  // wrong capitalization
    assertFalse(insDut.getAttributeNamed(dut, " Alfred").isPresent()); // extra prefix
    assertFalse(insDut.getAttributeNamed(dut, "Alfreds").isPresent()); // extra suffix
    assertSame(node1, insDut.getAttributeNamed(dut, "Alfred").get());
    
    // d.2 More than one proper child
    final Node node2 = dut.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "Fiedler"));
    final Node node3 = dut.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "bar"));
    assertSame(node1, insDut.getAttributeNamed(dut, "Alfred").get());  // match in 1st proper child
    assertSame(node2, insDut.getAttributeNamed(dut, "Fiedler").get()); // match in 2nd proper child
    assertSame(node3, insDut.getAttributeNamed(dut, "bar").get());     // match in 3rd proper child
  } // end method */

  /**
   * Test method for {@link ClasspathFile#getGradleScope(Node)}.
   */
  @Test
  void test_getGradleScope__Node() {
    // Test strategy:
    // --- a. minimal node with gradle scope plus value
    // --- b. minimal node with gradle scope without value
    // --- c. minimal node without gradle scope
    // --- d. node without appropriate children
    // --- e. node without children
    
    Node dut;
    
    // --- a. minimal node with gradle scope plus value
    final String valueA = "foo.bar.A";
    final Map<String, String> mapA = new LinkedHashMap<>();
    mapA.put("name",  "gradle_used_by_scope");
    mapA.put("value", valueA);
    dut = new Node(null, "item.a");
    dut.appendNode(ClasspathFile.NAME_GRAND, mapA);
    assertEquals(valueA, insDut.getGradleScope(dut));
    
    // --- b. minimal node with gradle scope without value
    final String empty = "";
    dut = new Node(null, "item.b");
    dut.appendNode(ClasspathFile.NAME_GRAND, Map.of("name",  "gradle_used_by_scope"));
    assertEquals(empty, insDut.getGradleScope(dut));
    
    // --- c. minimal node without gradle scope
    dut = new Node(null, "item.c");
    dut.appendNode(ClasspathFile.NAME_GRAND, Map.of("name",  "gradle_used_by_Scope"));
    assertEquals(empty, insDut.getGradleScope(dut));
    
    // --- d. node without appropriate children
    dut = new Node(null, "item.d");
    dut.appendNode("foo.bar");
    assertEquals(empty, insDut.getGradleScope(dut));
    
    // --- e. node without children
    assertEquals(empty, insDut.getGradleScope(new Node(null, "item.e")));
  } // end method */

  /**
   * Test method for {@link ClasspathFile#hasAttributeNamed(Node, String)}.
   */
  @Test
  void test_hasAttributeNamed__Node_String() {
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
    assertFalse(insDut.hasAttributeNamed(dut, "foo"));

    // --- b. Node with children not named "attribute" SHALL return false
    dut.appendNode("Attribute", mapItem); // wrong capitalization
    dut.appendNode("attributes", mapItem); // extra characters
    assertFalse(insDut.hasAttributeNamed(dut, "foo"));
    
    // --- c. Node with children named "attribute" but without proper attribute SHALL return false
    // c.1 child "attribute" without attributes
    dut.appendNode(ClasspathFile.NAME_GRAND);
    assertFalse(insDut.hasAttributeNamed(dut, "foo"));
    
    // c.2 child "attribute" with attributes
    dut.appendNode(ClasspathFile.NAME_GRAND, mapItem);
    // Note: assertions for c.2 are combined with assertions in d, see below
    
    // --- d. Node with children named "attribute" and proper attribute SHALL return true
    // d.1 Just one proper child
    assertFalse(insDut.hasAttributeNamed(dut, "alfred"));  // wrong capitalization
    assertFalse(insDut.hasAttributeNamed(dut, " Alfred")); // extra prefix
    assertFalse(insDut.hasAttributeNamed(dut, "Alfreds")); // extra suffix
    assertTrue(insDut.hasAttributeNamed(dut, "Alfred"));
    
    // d.2 More than one proper child
    dut.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "Fiedler"));
    dut.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "bar"));
    assertTrue(insDut.hasAttributeNamed(dut, "Alfred"));  // match in first  proper child
    assertTrue(insDut.hasAttributeNamed(dut, "Fiedler")); // match in second proper child
    assertTrue(insDut.hasAttributeNamed(dut, "bar"));     // match in third  proper child
  } // end method */

  /**
   * Test method for {@link ClasspathFile#hasNoAttributeModule(Node)}.
   */
  @Test
  void test_hasNoAttributeModule__Node() {
    // Test strategy:
    // --- a. "short" node, i.e. node with not enough information (returns always true)
    // --- b. node with just sufficient information
    // --- c. node triggering false by first  child
    // --- d. node triggering false by second child
    // --- e. node triggering false by third  child
    
    // --- a. "short" node, i.e. node with not enough information (returns always true)
    // a.1 empty node
    final Node nodeA = new Node(null, "a");
    assertTrue(insDut.hasNoAttributeModule(nodeA));
    
    // a.2 node with empty child
    final Node childA = nodeA.appendNode(ClasspathFile.NAME_CHILD);
    assertTrue(insDut.hasNoAttributeModule(nodeA));
    
    // a.3 node with empty grand-child
    Node grandA = childA.appendNode(ClasspathFile.NAME_GRAND);
    assertTrue(insDut.hasNoAttributeModule(nodeA));
    
    // a.4 node with non-empty grand-child, but inappropriate attributes
    childA.remove(grandA);
    grandA = childA.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "modul")); // not module
    // LOGGER.quiet("a.3: {}", nodeA);
    assertEquals(1, nodeA. children().size());
    assertEquals(1, childA.children().size());
    assertTrue(insDut.hasNoAttributeModule(nodeA));
    
    childA.remove(grandA);
    grandA = childA.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "mOdule")); // not module
    assertTrue(insDut.hasNoAttributeModule(nodeA));
    
    childA.remove(grandA);
    grandA = childA.appendNode(ClasspathFile.NAME_GRAND, Map.of("naMe", "module")); // not name
    assertTrue(insDut.hasNoAttributeModule(nodeA));
    
    // --- b. node with just sufficient information
    childA.remove(grandA);
    grandA = childA.appendNode(ClasspathFile.NAME_GRAND, Map.of("name", "module"));
    assertFalse(insDut.hasNoAttributeModule(nodeA));
    
    // --- c. node triggering false by first  child
    // two (slightly) different nodes, one returns true, the other false
    assertTrue(insDut.hasNoAttributeModule(hnam(1, Map.of("name", "modUle"))));
    assertFalse(insDut.hasNoAttributeModule(hnam(1, Map.of("name", "module"))));
    
    // --- d. node triggering false by second child
    assertTrue(insDut.hasNoAttributeModule(hnam(2, Map.of("name", "modUle"))));
    assertFalse(insDut.hasNoAttributeModule(hnam(2, Map.of("name", "module"))));
    
    // --- e. node triggering false by third  child
    assertTrue(insDut.hasNoAttributeModule(hnam(3, Map.of("name", "modUle"))));
    assertFalse(insDut.hasNoAttributeModule(hnam(3, Map.of("name", "module"))));
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
   * Test method for {@link ClasspathFile#improveEclipseClasspathFile(Node)}.
   */
  @Test
  void test_improveEclipseClasspathFile__Node() {
    // Test strategy:
    // ... assertion 1: method putJreOnModulePath(Node) works as intended
    // --- a. JRE, rootNode with a bunch of entries and entries differing slightly
    
    // --- a. JRE, rootNode with a bunch of entries and entries differing slightly
    final Node rootNode = new Node(null, "root");
    
    // a.1: difference in name of item
    final Map<String, String> mapA1 = new LinkedHashMap<>();
    mapA1.put("path", "prefix" + "JRE_CONTAINER" + "suffix");
    mapA1.put("kind", "con");
    rootNode.appendNode("classpathentry", mapA1); // ok
    rootNode.appendNode("classpathEntry", mapA1); // not classpathentry
    
    // --- improve
    insDut.improveEclipseClasspathFile(rootNode);
    
    // --- check
    assertEquals(
        "root[attributes={}; value=["
        // a.1, begin
        +   "classpathentry[attributes={path=prefixJRE_CONTAINERsuffix, kind=con}; value=["
        +     "attributes[attributes={}; value=["
        +       "attribute[attributes={name=module, value=true}; value=[]]"
        +     "]]]"
        +   "], "
        +   "classpathEntry[attributes={path=prefixJRE_CONTAINERsuffix, kind=con}; value=[]]"
        + "]]",
        rootNode.toString()
    );
  } // end method */

  /**
   * Test method for {@link ClasspathFile#isJre(Node)}.
   */
  @Test
  void test_isJre__Node() {
    // Test strategy:
    // --- a.  kind of "con"   and   path contains "JRE_CONTAINER"
    // --- b. !kind of "con"   but   path contains "JRE_CONTAINER"
    // --- c.  kind of "con"   but  !path contains "JRE_CONTAINER"
    
    // --- a.  kind of "con"   and   path contains "JRE_CONTAINER"
    assertTrue(insDut.isJre(new Node(null, "root", Map.of(
        "kind", "con",
        "path", ClasspathFile.NAME_JRE // minimalistic
    ))));
    assertTrue(insDut.isJre(new Node(null, "root", Map.of(
        "kind", "con",
        "path", "prefix" + ClasspathFile.NAME_JRE
    ))));
    assertTrue(insDut.isJre(new Node(null, "root", Map.of(
        "kind", "con",
        "path", ClasspathFile.NAME_JRE + "suffix"
    ))));
    
    // --- b. !kind of "con"   but   path contains "JRE_CONTAINER"
    assertFalse(insDut.isJre(new Node(null, "root", Map.of(
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
              insDut.isJre(new Node(null, "root", Map.of(
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
              insDut.isJre(new Node(null, "root", Map.of(
                  "kind", con,
                  "path", ClasspathFile.NAME_JRE
              )))
          );
        }); // end forEach(con -> ...)
    
    // --- c.  kind of "con"   but  !path contains "JRE_CONTAINER"
    assertFalse(insDut.isJre(new Node(null, "root", Map.of(
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
              insDut.isJre(new Node(null, "root", Map.of(
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
              insDut.isJre(new Node(null, "root", Map.of(
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
  void test_isKindOf__Node_String() {
    // Test strategy:
    // --- a. node without attributes
    // --- b. node without attribute "kind"
    // --- c. node with attribute "kind" and different values for that attribute
    
    // --- a. node without attributes
    assertFalse(insDut.isKindOf(new Node(null, "a"), "con"));
    
    // --- b. node without attribute "kind"
    assertFalse(insDut.isKindOf(
        new Node(null, "a", Map.of("Kind", "con")), // wrong capitalization
        "con"
    ));
    assertFalse(insDut.isKindOf(
        new Node(null, "a", Map.of("kinda", "con")), // suffix
        "con"
    ));
    assertFalse(insDut.isKindOf(
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
          assertTrue(insDut.isKindOf(new Node(null, "root", Map.of("kind", kind)), kind));
          
          // node with lots of attributes, but none fitting
          final Node dut = new Node(null, "root" + kind, attributes);
          variants.stream()
              .forEach(i -> assertFalse(insDut.isKindOf(dut, i)));
        }); // end forEach(kind -> ...)
  } // end method */
  
  /**
   * Test method for {@link ClasspathFile#markTest(Node)}.
   */
  @Test
  void test_markTest__Node() {
    // ... assertion: method checkGradleScopeTest(Node) works as expected
    //     => not much to check hereafter
    
    // Test strategy:
    // --- a. items with improper name are not changed
    // --- b. items with proper name are changed
    
    final Node root = new Node(null, "root");
    final Map<String, String> mapProperScope = new LinkedHashMap<>();
    mapProperScope.put("name", "gradle_used_by_scope");
    mapProperScope.put("value", "test");
    
    // --- a. items with improper name are not changed
    root.appendNode("classPathentry") // wrong capitalization
        .appendNode(ClasspathFile.NAME_CHILD)
        .appendNode(ClasspathFile.NAME_GRAND, mapProperScope);
    
    // --- b. items with proper name are changed
    root.appendNode("classpathentry")
        .appendNode(ClasspathFile.NAME_CHILD)
        .appendNode(ClasspathFile.NAME_GRAND, mapProperScope);
    
    insDut.markTest(root);
    assertEquals(
        "root[attributes={}; value=["
        +   "classPathentry[attributes={}; value=["
        +     "attributes[attributes={}; value=["
        +       "attribute[attributes={name=gradle_used_by_scope, value=test}; value=[]]"
        +     "]]"
        +   "]], "
        +   "classpathentry[attributes={}; value=["
        +     "attributes[attributes={}; value=["
        +       "attribute[attributes={name=gradle_used_by_scope, value=test}; value=[]], "
        +       "attribute[attributes={name=test, value=true}; value=[]]"
        +     "]]"
        +   "]]"
        + "]]",
        root.toString()
    );
  } // end method */

  /**
   * Test method for {@link ClasspathFile#moveToModulePath(Node)}.
   */
  @Test
  void test_moveToModulePath__Node() {
    // Test strategy:
    // --- a. empty node
    // --- b. node with children none named "attributes"
    // --- c. node with several children named "attributes"
    // --- d. node already moved
    
    Node dut;
    
    // --- a. empty node
    dut = new Node(null, "rootA");
    insDut.moveToModulePath(dut);
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
    insDut.moveToModulePath(dut);
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
    insDut.moveToModulePath(dut);
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
    insDut.moveToModulePath(dut);
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
    insDut.moveToModulePath(dut);
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
    insDut.moveToModulePath(dut);
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
    insDut.moveToModulePath(dut);
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
   * Test method for {@link ClasspathFile#putJreOnModulePath(Node)}.
   */
  @Test
  void test_putJreOnModulePath__Node() {
    // Test strategy:
    // ... assertion 1: method isJre(Node)                works as intended
    // ... assertion 2: method hasNoAttributeModule(Node) works as intended
    // ... assertion 3: method moveToModulePath(Node)     works as intended
    // --- a. rootNode with a bunch of entries and entries differing slightly
    
    // --- a. rootNode with a bunch of entries and entries differing slightly
    final Node rootNode = new Node(null, "root");
    
    // a.1: difference in name of item
    final Map<String, String> mapA1 = new LinkedHashMap<>();
    mapA1.put("path", "prefix" + "JRE_CONTAINER" + "suffix");
    mapA1.put("kind", "con");
    rootNode.appendNode("classpathentry", mapA1); // ok
    rootNode.appendNode("classpathEntry", mapA1); // not classpathentry
    
    // --- improve
    insDut.putJreOnModulePath(rootNode);
    
    // --- check
    assertEquals(
        "root[attributes={}; value=["
        // a.1, begin
        +   "classpathentry[attributes={path=prefixJRE_CONTAINERsuffix, kind=con}; value=["
        +     "attributes[attributes={}; value=["
        +       "attribute[attributes={name=module, value=true}; value=[]]"
        +     "]]]"
        +   "], "
        +   "classpathEntry[attributes={path=prefixJRE_CONTAINERsuffix, kind=con}; value=[]]"
        + "]]",
        rootNode.toString()
    );
  } // end method */
} // end class