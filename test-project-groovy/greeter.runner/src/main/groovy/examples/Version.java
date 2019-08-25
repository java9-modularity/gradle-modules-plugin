package examples;

public class Version {
    private static final int value = 3;

    public static String getValue() {
        return value + "." + MinorVersion.getMinorAndPatchValue();
    }
}
