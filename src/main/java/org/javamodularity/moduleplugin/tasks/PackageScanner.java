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

public class PackageScanner extends ClassVisitor {
    private static final Logger LOGGER = Logging.getLogger(PackageScanner.class);

    private final Set<String> packages = new TreeSet<>();

    public PackageScanner() {
        super(Opcodes.ASM7);
    }

    public Set<String> getPackages() {
        return packages;
    }

    public void scan(File dir) {
        LOGGER.debug("Scanning packages in " + dir);
        if(!dir.isDirectory()) {
            LOGGER.debug("Not a directory: " + dir);
            return;
        }
        try(Stream<Path> entries = Files.walk(dir.toPath())) {
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
        addClass(name);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    private static boolean isValidClassFileReference(String path) {
        if(!path.endsWith(".class")) return false;
        String name = path.substring(0, path.length() - ".class".length());
        String[] tokens = name.split("[./\\\\]");
        if(tokens.length == 0) return false;
        return Utilities.isJavaIdentifier(tokens[tokens.length - 1]);
    }


    private void addClass(String className) {
        if(className == null || className.isEmpty()) return;
        String pkg = getPackageName(adjust(className));
        if(!pkg.isEmpty()) packages.add(pkg);
    }

    private static String getPackageName(String className) {
        int pos = className.lastIndexOf('.');
        if(pos < 0) throw new IllegalArgumentException("Cannot find '.' in " + className);
        return className.substring(0, pos);
    }

    private static String adjust(String s) {
        if(s == null || s.isBlank()) return "";
        while(!Character.isJavaIdentifierStart(s.charAt(0))){
            s = s.substring(1);
            if(s.isEmpty()) return "";
        }
        while(!Character.isJavaIdentifierPart(s.charAt(s.length() - 1))) {
            s = s.substring(0, s.length()-1);
            if(s.isEmpty()) return "";
        }
        return s.replace('/', '.');
    }
}
