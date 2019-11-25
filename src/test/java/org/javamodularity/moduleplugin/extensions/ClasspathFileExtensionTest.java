package org.javamodularity.moduleplugin.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Class testing {@link ClasspathFileExtension}.
 * 
 * <p><b>Note:</b> This class performs white-box-tests.
 * 
 * @author <a href="mailto:alfred.65.fiedler@gmail.com">Dr.-Ing. Alfred Fiedler</a>
 */
final class ClasspathFileExtensionTest {
  /**
   * Logger.
   *
  private static final Logger LOGGER = Logging.getLogger(ClasspathFileTest.class); // */
  
  /**
   * Device under test.
   */
  private Project insDut; // */
  
  /** Method executed before other tests. */
  @BeforeAll
  static void setUpBeforeClass() {
    // Test strategy:
    // --- a. check default value
    assertFalse(ClasspathFileExtension.DEFAULT_VALUE);
  } // end method */
  
  /** Method executed after other tests. */
  @AfterAll
  static void tearDownAfterClass() {
    // intentionally empty
  } // end method */
  
  /** Method executed before each test. */
  @BeforeEach
  void setUp() {
    insDut = ProjectBuilder.builder().build();
  } // end method */
  
  /** Method executed after each test. */
  @AfterEach
  void tearDown() {
    // intentionally empty
  } // end method */
  
  /**
   * Test method for {@link ClasspathFileExtension#ClasspathFileExtension(Project)}.
   */
  @Test
  void test_ClasspathFileExtension__Project() {
    // Test strategy:
    // --- a. check that instances could be created
    // --- b. check that default value is properly set
    
    // --- a. check that instances could be created
    final ClasspathFileExtension dut = new ClasspathFileExtension(insDut);
    assertNotNull(dut);
    
    // --- b. check that default value is properly set
    assertEquals(
        ClasspathFileExtension.DEFAULT_VALUE,
        dut.getImproveClasspathFile().get()
    );
  } // end method */
  
  /**
   * Test method for {@link ClasspathFileExtension#getImproveClasspathFile()}.
   */
  @Test
  void test_getImproveClasspathFile() {
    // Test strategy:
    // --- a. check that method under test doesn't return null
    // --- b. check that method under test returns always the same object
    // --- c. check that it is possible to change the value
    
    final ClasspathFileExtension dut = new ClasspathFileExtension(insDut);
    
    // --- a. check that method under test doesn't return null
    final Property<Boolean> property = dut.getImproveClasspathFile();
    assertNotNull(property);
    
    // --- b. check that method under test returns always the same object
    assertSame(property, dut.getImproveClasspathFile());
    
    // --- c. check that it is possible to change the value
    List.of(true, false).stream()
        .forEach(i -> {
          property.set(i);
          assertEquals(i, dut.getImproveClasspathFile().get());
        }); // end forEach(i -> ...)
  } // end method */
} // end class