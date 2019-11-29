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

final class ClasspathFileTest2 {
  
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
   * Test method for {@link ClasspathFile#getGradleScop(Node)}.
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
    assertEquals(valueA, insDut.getGradleScop(dut));
    
    // --- b. minimal node with gradle scope without value
    final String empty = "";
    dut = new Node(null, "item.b");
    dut.appendNode(ClasspathFile.NAME_GRAND, Map.of("name",  "gradle_used_by_scope"));
    assertEquals(empty, insDut.getGradleScop(dut));
    
    // --- c. minimal node without gradle scope
    dut = new Node(null, "item.c");
    dut.appendNode(ClasspathFile.NAME_GRAND, Map.of("name",  "gradle_used_by_Scope"));
    assertEquals(empty, insDut.getGradleScop(dut));
    
    // --- d. node without appropriate children
    dut = new Node(null, "item.d");
    dut.appendNode("foo.bar");
    assertEquals(empty, insDut.getGradleScop(dut));
    
    // --- e. node without children
    assertEquals(empty, insDut.getGradleScop(new Node(null, "item.e")));
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
} // end class