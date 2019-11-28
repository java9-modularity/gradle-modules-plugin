package foo.libu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
final class LibraryTest5 {
  @Test void test_Method() {
    Library dut = new Library();
    assertEquals(21, dut.method(), "expecting 21.c");
  }
}