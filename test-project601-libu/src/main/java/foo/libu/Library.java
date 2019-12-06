package foo.libu;

import foo.lima.LibraryA;
import foo.limu.LibraryU;

@SuppressWarnings("javadoc")
public class Library {
  private final LibraryA libA = new LibraryA();
  private final LibraryU libU = new LibraryU();
  public int method() {
    return libA.method() + libU.method();
  }
}