package foo.libu;

import foo.limu.LibraryU;

@SuppressWarnings("javadoc")
public class Library {
  private final LibraryU lib = new LibraryU();
  public int method() {
    return lib.method();
  }
}