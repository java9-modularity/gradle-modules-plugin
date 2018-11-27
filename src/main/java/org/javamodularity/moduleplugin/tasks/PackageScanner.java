package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.nio.file.Files;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for retrieving and collecting package names from source files (Java, Kotlin, Groovy, or Scala).
 */
public class PackageScanner {
    private static final Logger LOGGER = Logging.getLogger(PackageScanner.class);

    private static final String WS = "[ \t\r\n\\u000C]";
    private static final String LETTER = "[a-zA-Z$_]|[^\\u0000-\\u007F\\uD800-\\uDBFF]|[\\uD800-\\uDBFF]|[\\uDC00-\\uDFFF]";
    private static final String LETTER_OR_DIGIT = LETTER + "|[0-9]";
    private static final String IDENTIFIER = "((" + LETTER + ")(" + LETTER_OR_DIGIT + ")*)";
    private static final String QUALIFIED_NAME = IDENTIFIER + "(\\." + IDENTIFIER + ")*";

    private static final String LINE_COMMENT = "//[^\r\n]*";
    private static final String MULTILINE_COMMENT = "/\\*.*?\\*/";
    private static final String IGNORE =  "(" + WS + "|" + LINE_COMMENT + "|" + MULTILINE_COMMENT + ")*";

    private static final String PACKAGE_DECLARATION = "(?s)" + IGNORE + "package" + IGNORE + "(?<PACKAGE>" + QUALIFIED_NAME + ").*?";

    private static final Pattern PATTERN = Pattern.compile(PACKAGE_DECLARATION);

    private final Set<String> packages = new TreeSet<>();

    public Set<String> getPackages() {
        return packages;
    }

    /**
     * Determines the package name of the specified source file and adds it to the {@link #packages} set.
     * @param file the file to be scanned
     * @return the package name, or null if the package declaration is missing or an error occurred.
     */
    public String scan(File file) {
        if(!file.isFile()) {
            LOGGER.warn("Not a source file: " + file);
        } else {
            try {
                String text = new String(Files.readAllBytes(file.toPath()));
                Matcher matcher = PATTERN.matcher(text);
                if(matcher.matches()) {
                    String packageName = matcher.group("PACKAGE");
                    packages.add(packageName);
                    return packageName;
                } else {
                    LOGGER.warn("Package declaration not found in: " + file);
                }
            } catch (Exception e) {
                LOGGER.warn("Cannot scan " + file);
                LOGGER.debug("Scan error", e);
            }
        }
        return null;
    }
}
