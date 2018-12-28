package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.file.FileCollection;

import java.io.File;
import java.util.*;
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

    public List<String> configure(FileCollection classpath) {
        List<String> args = new ArrayList<>();

        config.forEach(patch -> {
                    String[] split = patch.split("=");

                    String asPath = classpath.filter(jar -> jar.getName().endsWith(split[1])).getAsPath();

                    if (asPath.length() > 0) {
                        args.add("--patch-module");
                        args.add(split[0] + "=" + asPath);
                    }
                }
        );

        return args;
    }

    public Set<String> getJars() {
        return indexByJar.keySet();
    }

    public boolean isUnpatched(File jar) {
        return !indexByJar.containsKey(jar.getName());
    }

    @Override
    public String toString() {
        return "PatchModuleExtension{" +
                "config=" + config +
                '}';
    }
}
