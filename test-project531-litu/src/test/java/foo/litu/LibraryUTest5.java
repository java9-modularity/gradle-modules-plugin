package foo.litu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
final class LibraryUTest5 {
  @Test void test_Method() {
    LibraryU dut = new LibraryU();
    assertEquals(21, dut.method(), "expecting 21.c");
  }
}