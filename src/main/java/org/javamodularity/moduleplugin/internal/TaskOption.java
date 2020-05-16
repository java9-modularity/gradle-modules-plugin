package org.javamodularity.moduleplugin.internal;

import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores a flag and its value that can be used as: compiler args, JVM args, and Javadoc options.
 */
public final class TaskOption {

    /**
     * E.g. {@code --add-modules}.
     */
    private final String flag;
    /**
     * E.g. {@code java.sql,my.module}.
     */
    private final String value;

    public TaskOption(String flag, String value) {
        this.flag = flag;
        this.value = value;
        this.validate();
    }

    private void validate() {
        if (!flag.startsWith("--")) {
            throw new IllegalArgumentException("Invalid flag: " + flag);
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
    }

    public String getFlag() {
        return flag;
    }

    public String getValue() {
        return value;
    }

    /**
     * Javadoc takes options without the initial hyphen.
     *
     * @return the Javadoc flag
     * @see org.gradle.external.javadoc.internal.JavadocOptionFileWriterContext#writeOptionHeader(String)
     */
    public String getJavadocFlag() {
        return flag.substring(1);
    }

    //region MUTATE
    public void mutateArgs(List<String> args) {
        args.add(flag);
        args.add(value);
    }

    public void mutateOptions(CoreJavadocOptions options) {
        String javadocFlag = getJavadocFlag();
        if(javadocFlag.equals("-module-path") && GradleVersion.current().compareTo(GradleVersion.version("6.4")) >= 0) {
            String[] paths = value.split(System.getProperty("path.separator"));
            List<File> modulePath = Arrays.stream(paths).map(File::new).collect(Collectors.toList());
            options.modulePath(modulePath);
        } else {
            options.addStringOption(javadocFlag, value);
        }
    }
    //endregion
}
