package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.file.FileCollection;
import org.javamodularity.moduleplugin.internal.PatchModuleResolver;

import java.io.File;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class PatchModuleExtension {
    private List<String> config = new ArrayList<>();
    private Map<String, String> indexByJar = new HashMap<>();

    public List<String> getConfig() {
        return config;
    }

    public void setConfig(List<String> config) {
        this.config = config;
        indexByJar = config.stream().map(s -> s.split("=")).collect(Collectors.toMap(c -> c[1], c -> c[0]));
    }

    public PatchModuleResolver resolve(FileCollection classpath) {
        return resolve(jarName -> classpath.filter(jar -> jar.getName().endsWith(jarName)).getAsPath());
    }

    public PatchModuleResolver resolve(UnaryOperator<String> jarNameResolver) {
        return new PatchModuleResolver(this, jarNameResolver);
    }

    public Set<String> getJars() {
        return indexByJar.keySet();
    }

    public boolean isUnpatched(File jar) {
        return !indexByJar.containsKey(jar.getName());
    }

    public String getUnpatchedClasspathAsPath(FileCollection classpath) {
        return classpath.filter(this::isUnpatched).getAsPath();
    }

    @Override
    public String toString() {
        return "PatchModuleExtension{" +
                "config=" + config +
                '}';
    }
}
