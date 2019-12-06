package foo.lima;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
final class LibraryATest5 {
  @Test void test_Method() {
    LibraryA dut = new LibraryA();
    assertEquals(1, dut.method(), "expecting 1.a");
  }
}