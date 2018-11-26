package org.javamodularity.moduleplugin.tasks;

import org.codehaus.groovy.tools.Utilities;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * A class visitor that collects the packages of the visited classes into a set.
 */
public class PackageScanner extends ClassVisitor {
    private static final Logger LOGGER = Logging.getLogger(PackageScanner.class);

    private final Set<String> packages = new TreeSet<>();

    public PackageScanner() {
        super(Opcodes.ASM7);
    }

    public Set<String> getPackages() {
        return packages;
    }

    /**
     * For each Java class file in the file tree of the specified directory retrieves its package and adds it to the {@link #packages} set.
     * @param dir the directory to be scanned
     */
    public void scan(File dir) {
        LOGGER.debug("Scanning packages in " + dir);
        if(!dir.isDirectory()) {
            LOGGER.debug("Not a directory: " + dir);
            return;
        }
        try(Stream<Path> entries = Files.walk(dir.toPath())
                .filter(entry -> entry.toFile().isFile())) {
            entries.forEach(entry -> {
                String path = entry.toString();
                if(isValidClassFileReference(path)) {
                    try(InputStream is = Files.newInputStream(entry)) {
                        ClassReader cr = new ClassReader(is);
                        cr.accept(this, 0);
                    } catch (Exception e) {
                        throw new GradleException("Failed to analyze " + path, e);
                    }
                }
            });
        } catch (IOException e) {
            throw new GradleException("Failed to scan " + dir, e);
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        addPackageOf(name);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * Checks if the {@code path} parameter represents a Java class file.
     */
    private static boolean isValidClassFileReference(String path) {
        if(!path.endsWith(".class")) return false;
        String name = path.substring(0, path.length() - ".class".length());
        String[] tokens = name.split("[./\\\\]");
        if(tokens.length == 0) return false;
        return Utilities.isJavaIdentifier(tokens[tokens.length - 1]);
    }
    
    /**
     * Adds the package of the fully qualified {@code className} parameter to the {@link #packages} set.
     */
    private void addPackageOf(String className) {
        if(className == null || className.isEmpty()) return;
        String pkg = getPackageName(className);
        if(!pkg.isEmpty()) packages.add(pkg);
    }

    /**
     * Retrieves the package of the fully qualified {@code className} parameter.
     */
    private static String getPackageName(String className) {
        String dottedClassName = className.replace('/', '.');
        int pos = dottedClassName.lastIndexOf('.');
        if(pos < 0) throw new IllegalArgumentException("Cannot retrieve the package of " + className);
        return dottedClassName.substring(0, pos);
    }
}
